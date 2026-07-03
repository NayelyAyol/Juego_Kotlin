package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.CountDownTimer
import android.view.View
import kotlin.random.Random

data class Obstaculo(var x: Float, var y: Float, val radio: Float)

class BallView(
    context: Context,
    private val colorBola: Int = Color.parseColor("#E53935"),
    private val radioBola: Float = 60f
) : View(context) {

    private var ballX = -1f
    private var ballY = -1f

    private var puntaje = 0
    private var vidas = 3
    private var juegoTerminado = false
    private var flashTimer = 0

    private val obstaculos = mutableListOf<Obstaculo>()

    // Controla cuántos árboles deberían existir según el puntaje actual.
    // Empieza en -1 para forzar la primera generación al iniciar.
    private var cantidadObjetivo = -1
    private val cantidadBase = 3
    private val cantidadMaxima = 8

    /** Callback que se invoca cuando el jugador pierde todas las vidas. */
    var onGameOver: ((Int) -> Unit)? = null

    private val paintBola = Paint().apply { isAntiAlias = true }
    private val paintBolaBrillo = Paint().apply {
        isAntiAlias = true
        color = Color.argb(120, 255, 255, 255)
    }

    // --- Paints del árbol (obstáculo) ---
    private val paintTronco = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#6D4C36")
    }
    private val paintCopaOscura = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#2E7D32")
    }
    private val paintCopaClara = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#4CAF50")
    }

    private val paintTexto = Paint().apply {
        color = Color.WHITE
        textSize = 38f
        isAntiAlias = true
        isFakeBoldText = true
    }

    private val paintPanel = Paint().apply {
        isAntiAlias = true
        color = Color.argb(140, 21, 40, 66)
    }

    private val paintInstrucciones = Paint().apply {
        color = Color.WHITE
        textSize = 32f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val paintBorde = Paint().apply {
        color = Color.parseColor("#FF5252")
        style = Paint.Style.STROKE
        strokeWidth = 14f
    }

    private var mostrarAlertaBorde = false
    private var fondoShader: Shader? = null

    // Suma 1 punto por cada segundo que el jugador sobrevive.
    private val timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            if (!juegoTerminado) {
                puntaje += 1
                invalidate()
            }
        }
        override fun onFinish() {}
    }

    init {
        timer.start()
    }

    /**
     * Dificultad progresiva: la cantidad de árboles crece con el puntaje
     * (cada 10 puntos aparece uno más), hasta un máximo de [cantidadMaxima].
     * No reinicia los árboles ya puestos: solo agrega los que faltan, así
     * el jugador no pierde referencia de golpe cuando sube el nivel.
     */
    private fun actualizarObstaculos() {
        if (width == 0 || height == 0) return

        val cantidadDeseada = (cantidadBase + puntaje / 10).coerceAtMost(cantidadMaxima)
        if (cantidadDeseada == cantidadObjetivo) return
        cantidadObjetivo = cantidadDeseada

        while (obstaculos.size < cantidadDeseada) {
            val r = 46f
            val x = Random.nextInt(r.toInt(), (width - r).toInt()).toFloat()
            val y = Random.nextInt((height * 0.35).toInt(), (height - r).toInt()).toFloat()
            obstaculos.add(Obstaculo(x, y, r))
        }
    }

    fun updateBall(sensorX: Float, sensorY: Float) {
        if (width == 0 || height == 0 || juegoTerminado) return

        if (ballX < 0) {
            ballX = width / 2f
            ballY = height / 2f
        }

        actualizarObstaculos()

        ballX -= sensorX * 6
        ballY += sensorY * 6

        // Detección de choque con los bordes de la pantalla.
        mostrarAlertaBorde = ballX <= radioBola || ballX >= width - radioBola ||
                ballY <= radioBola || ballY >= height - radioBola

        ballX = ballX.coerceIn(radioBola, width - radioBola)
        ballY = ballY.coerceIn(radioBola, height - radioBola)

        // Detección de choque con los árboles.
        for (obstaculo in obstaculos) {
            val dx = ballX - obstaculo.x
            val dy = ballY - obstaculo.y
            val distancia = Math.sqrt((dx * dx + dy * dy).toDouble())
            if (distancia < radioBola + obstaculo.radio) {
                vidas -= 1
                flashTimer = 10
                obstaculo.x = Random.nextInt(60, (width - 60)).toFloat()
                obstaculo.y = Random.nextInt((height * 0.35).toInt(), (height - 60)).toFloat()
                if (vidas <= 0) {
                    juegoTerminado = true
                    timer.cancel()
                    onGameOver?.invoke(puntaje)
                }
            }
        }

        if (flashTimer > 0) flashTimer--

        invalidate()
    }

    /** Dibuja un árbol: tronco + dos capas de copa para dar sensación de volumen. */
    private fun dibujarArbol(canvas: Canvas, cx: Float, cy: Float, radio: Float) {
        val anchoTronco = radio * 0.34f
        val altoTronco = radio * 1.15f
        canvas.drawRoundRect(
            cx - anchoTronco / 2f, cy - altoTronco * 0.1f,
            cx + anchoTronco / 2f, cy + altoTronco,
            8f, 8f, paintTronco
        )
        // Copa: tres círculos superpuestos para simular follaje.
        val copaY = cy - radio * 0.35f
        canvas.drawCircle(cx, copaY, radio, paintCopaOscura)
        canvas.drawCircle(cx - radio * 0.5f, copaY + radio * 0.25f, radio * 0.75f, paintCopaOscura)
        canvas.drawCircle(cx + radio * 0.5f, copaY + radio * 0.25f, radio * 0.75f, paintCopaOscura)
        canvas.drawCircle(cx - radio * 0.2f, copaY - radio * 0.2f, radio * 0.6f, paintCopaClara)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (fondoShader == null && width > 0 && height > 0) {
            fondoShader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                Color.parseColor("#81D4FA"), Color.parseColor("#C8E6C9"),
                Shader.TileMode.CLAMP
            )
        }
        val fondoPaint = Paint().apply { shader = fondoShader }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), fondoPaint)

        // "Pasto" en la franja inferior para dar contexto a los árboles.
        canvas.drawRect(0f, height * 0.82f, width.toFloat(), height.toFloat(), Paint().apply {
            color = Color.parseColor("#AED581")
        })

        for (obstaculo in obstaculos) {
            dibujarArbol(canvas, obstaculo.x, obstaculo.y, obstaculo.radio)
        }

        if (ballX >= 0) {
            val colorActual = if (flashTimer > 0) Color.parseColor("#FFC107") else colorBola
            paintBola.shader = RadialGradient(
                ballX - radioBola * 0.3f, ballY - radioBola * 0.3f, radioBola * 1.3f,
                Color.WHITE, colorActual, Shader.TileMode.CLAMP
            )
            canvas.drawCircle(ballX, ballY, radioBola, paintBola)
            canvas.drawCircle(ballX - radioBola * 0.35f, ballY - radioBola * 0.35f, radioBola * 0.22f, paintBolaBrillo)
        }

        if (mostrarAlertaBorde) {
            canvas.drawRect(6f, 6f, width - 6f, height - 6f, paintBorde)
        }

        // Panel superior con puntaje, vidas y coordenadas.
        canvas.drawRoundRect(24f, 24f, 420f, 210f, 24f, 24f, paintPanel)
        canvas.drawText("Puntaje: $puntaje", 44f, 78f, paintTexto)
        canvas.drawText("Vidas: $vidas", 44f, 128f, paintTexto)
        canvas.drawText("X: ${ballX.toInt()}  Y: ${ballY.toInt()}", 44f, 178f, paintTexto)

        // Barra de instrucciones inferior.
        canvas.drawRoundRect(24f, height - 96f, width - 24f, height - 24f, 20f, 20f, paintPanel)
        canvas.drawText(
            "Inclina el celular para esquivar los árboles",
            width / 2f, height - 52f, paintInstrucciones
        )
    }

    fun detener() {
        timer.cancel()
    }
}
