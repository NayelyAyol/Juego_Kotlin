package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Pantalla de inicio responsive: elegir color, tamaño y ver instrucciones. */
class MainMenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MenuScreen { colorElegido, tamanoElegido ->
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("BALL_COLOR", colorElegido)
                intent.putExtra("BALL_SIZE", tamanoElegido)
                startActivity(intent)
            }
        }
    }
}

@Composable
fun MenuScreen(onStart: (Int, Float) -> Unit) {
    var colorSeleccionado by remember { mutableStateOf(Color.parseColor("#E53935")) }
    var tamanoSeleccionado by remember { mutableStateOf(60f) }

    val colores = listOf(
        Color.parseColor("#E53935"),
        Color.parseColor("#1E88E5"),
        Color.parseColor("#43A047"),
        Color.parseColor("#8E24AA")
    )
    val tamanos = listOf("Pequeña" to 40f, "Mediana" to 60f, "Grande" to 85f)

    // BoxWithConstraints nos da el ancho/alto real disponible en CUALQUIER
    // dispositivo (celular chico, celular grande, tablet, horizontal, etc.)
    // para poder adaptar tamaños en vez de dejarlos fijos.
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(ComposeColor(0xFF1565C0), ComposeColor(0xFF90CAF9)))
            )
    ) {
        val anchoPantalla = maxWidth
        val esPantallaAncha = anchoPantalla > 600.dp

        // La tarjeta ocupa el 92% del ancho en celulares, pero nunca supera
        // 420dp para que no se vea gigante ni deforme en tablets.
        val anchoTarjeta = if (esPantallaAncha) 420.dp else anchoPantalla * 0.92f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.width(anchoTarjeta)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Bola Inclinada",
                        fontSize = if (esPantallaAncha) 32.sp else 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = ComposeColor(0xFF1565C0),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Inclina el celular para mover la bola, esquiva los árboles " +
                                "y suma puntos por cada segundo que sobrevivas.",
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        color = ComposeColor(0xFF424242)
                    )

                    Spacer(Modifier.height(20.dp))
                    Text("Color de la bola", fontWeight = FontWeight.SemiBold)

                    // FlowRow manual con Row + wrap: en pantallas angostas los
                    // círculos de color se acomodan igual sin desbordar.
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                    ) {
                        colores.forEach { colorInt ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(ComposeColor(colorInt), RoundedCornerShape(50))
                                    .then(
                                        if (colorSeleccionado == colorInt)
                                            Modifier.border(3.dp, ComposeColor.Black, RoundedCornerShape(50))
                                        else Modifier
                                    )
                                    .clickable { colorSeleccionado = colorInt }
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text("Tamaño de la bola", fontWeight = FontWeight.SemiBold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                    ) {
                        tamanos.forEach { (nombre, valor) ->
                            FilterChip(
                                selected = tamanoSeleccionado == valor,
                                onClick = { tamanoSeleccionado = valor },
                                label = { Text(nombre) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(28.dp))
                    Button(
                        onClick = { onStart(colorSeleccionado, tamanoSeleccionado) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Iniciar Juego", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
