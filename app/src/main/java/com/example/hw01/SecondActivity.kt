package com.example.hw01

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

class SecondActivity : AppCompatActivity(), SensorEventListener {
    private val sensorManager: SensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    private lateinit var tiltView: TiltViewSecond

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tiltView = TiltViewSecond(this)
        tiltView.onOutOfBounds = { triggerFeedback() }
        setContentView(tiltView)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun triggerFeedback() {
        Log.d("Vibration", "triggerFeedback called")
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator.vibrate(
            VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            Log.d("onSensorChanged", "x: ${it.values[0]}, y: ${it.values[1]}, z: ${it.values[2]}")
        }
        tiltView.onSensorEvent(event)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    class TiltViewSecond(context: Context?) : View(context) {
        private val greenPaint: Paint = Paint()
        private val blackPaint: Paint = Paint()

        private var cX: Float = 0f
        private var cY: Float = 0f
        private var xCoord: Float = 0f
        private var yCoord: Float = 0f
        private var wasOutOfBounds = false

        var onOutOfBounds: (() -> Unit)? = null

        companion object {
            private const val SQUARE_HALF = 100f
            private const val LINE_HALF = 20f
        }

        init {
            greenPaint.color = Color.GREEN
            blackPaint.style = Paint.Style.STROKE
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            cX = w / 2f
            cY = h / 2f
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawRect(cX - SQUARE_HALF, cY - SQUARE_HALF, cX + SQUARE_HALF, cY + SQUARE_HALF, blackPaint)
            canvas.drawRect(cX + xCoord - SQUARE_HALF, cY + yCoord - SQUARE_HALF, cX + xCoord + SQUARE_HALF, cY + yCoord + SQUARE_HALF, greenPaint)
            canvas.drawLine(cX - LINE_HALF, cY, cX + LINE_HALF, cY, blackPaint)
            canvas.drawLine(cX, cY - LINE_HALF, cX, cY + LINE_HALF, blackPaint)
        }

        fun onSensorEvent(event: SensorEvent?) {
            yCoord = event!!.values[0] * 20
            xCoord = event.values[1] * 20

            val isOutOfBounds = abs(xCoord) > SQUARE_HALF || abs(yCoord) > SQUARE_HALF
            if (isOutOfBounds && !wasOutOfBounds) {
                onOutOfBounds?.invoke()
            }
            wasOutOfBounds = isOutOfBounds

            invalidate()
        }
    }
}
