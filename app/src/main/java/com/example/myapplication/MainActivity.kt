package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var ballView: BallView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Color y tamaño elegidos en el menú (Nivel básico).
        val colorSeleccionado = intent.getIntExtra("BALL_COLOR", Color.parseColor("#E53935"))
        val tamanoSeleccionado = intent.getFloatExtra("BALL_SIZE", 60f)

        ballView = BallView(this, colorSeleccionado, tamanoSeleccionado)
        setContentView(ballView)

        ballView.onGameOver = { puntajeFinal ->
            runOnUiThread { mostrarDialogoFinal(puntajeFinal) }
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private fun mostrarDialogoFinal(puntaje: Int) {
        AlertDialog.Builder(this)
            .setTitle("¡Juego terminado!")
            .setMessage("Chocaste demasiadas veces.\nPuntaje final: $puntaje")
            .setCancelable(false)
            .setPositiveButton("Reintentar") { _, _ ->
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Menú") { _, _ ->
                startActivity(Intent(this, MainMenuActivity::class.java))
                finish()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        ballView.updateBall(event.values[0], event.values[1])
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        ballView.detener()
    }
}
