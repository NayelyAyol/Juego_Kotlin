package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import kotlin.math.max
import kotlin.math.min

class BallView(context: Context) : View(context) {

    private var ballX = 300f
    private var ballY = 300f

    private val radius = 60f
    private var score = 0

    private val paint = Paint().apply {
        color = Color.RED
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 50f
    }

    fun updateBall(sensorX: Float, sensorY: Float) {

        if (width == 0 || height == 0) return

        ballX -= sensorX * 6
        ballY += sensorY * 6

        ballX = ballX.coerceIn(radius, width - radius)
        ballY = ballY.coerceIn(radius, height - radius)

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.rgb(240, 240, 240))

        canvas.drawCircle(ballX, ballY, radius, paint)

        canvas.drawText("X: $ballX Y: $ballY", 50f, 100f, textPaint)

        canvas.drawText("Score: $score", 50f, 180f, textPaint)

        canvas.drawText("Inclina el celular para mover la bola", 50f, 260f, textPaint)
    }
}