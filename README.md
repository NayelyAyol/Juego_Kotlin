# <h1 align="center"> 🎮 Bola Inclinada - Juego de Gravedad con Sensores en Android / Kotlin 💙 </h1>

## Descripción

Aplicación móvil desarrollada en **Kotlin** con **Android Views + Jetpack Compose**, creada como Reto Final de taller, que utiliza el **acelerómetro** del dispositivo para controlar una bola en pantalla, esquivar obstáculos y acumular puntaje mientras el celular se inclina en distintas direcciones.

La aplicación permite:

- Elegir el color de la bola antes de jugar
- Elegir el tamaño de la bola antes de jugar
- Leer instrucciones del juego en el menú y durante la partida
- Ver las coordenadas X/Y de la bola en tiempo real
- Esquivar obstáculos con forma de árbol generados dinámicamente
- Ganar puntos por cada segundo que se sobrevive
- Ver la dificultad aumentar de forma progresiva según el puntaje
- Detectar choques contra los bordes de la pantalla
- Detectar choques contra los obstáculos, con sistema de vidas
- Ver una pantalla de inicio (menú) antes de comenzar a jugar
- Ver un splash screen animado y personalizado al abrir la app

---

## Autor(a)

- _(Tu nombre aquí)_

---

## Tecnologías utilizadas

- Kotlin
- Android SDK (Views + Canvas)
- Jetpack Compose (Material 3)
- SensorManager / Sensor.TYPE_ACCELEROMETER
- Android Studio
- Core SplashScreen (`androidx.core:core-splashscreen`)

---

## Funcionalidades

### Nivel básico
- Color de la bola seleccionable desde el menú (rojo, azul, verde, morado)
- Tamaño de la bola seleccionable desde el menú (pequeña, mediana, grande)
- Texto con instrucciones visible en el menú y dentro del juego
- Coordenadas X/Y mostradas en tiempo real sobre la pantalla de juego

### Nivel intermedio
- Obstáculos con forma de **árbol** (tronco + copa con volumen), generados en posiciones aleatorias
- Cantidad de obstáculos **dinámica y progresiva**: empieza en 3 y aumenta 1 cada 10 puntos, hasta un máximo de 8
- Sistema de puntaje: +1 punto por cada segundo sobrevivido
- Sistema de vidas: 3 vidas, se pierde una por cada choque con un obstáculo
- Detección de choque con los bordes de la pantalla (marco rojo de aviso)
- Detección de choque con obstáculos (destello amarillo en la bola)
- Pantalla de Game Over con puntaje final y opciones **Reintentar** / **Menú**
- Pantalla de inicio (menú) con selección de color, tamaño e instrucciones

### Diseño / identidad visual
- Escenario tipo paisaje: cielo degradado + franja de pasto
- Bola con efecto de volumen (degradado radial + brillo)
- Panel de estadísticas (puntaje, vidas, coordenadas) e instrucciones en tarjetas semitransparentes
- Menú de inicio **responsive**: se adapta al ancho de cualquier dispositivo (celular chico, celular grande o tablet) usando `BoxWithConstraints` y scroll vertical
- Ícono de app adaptativo y personalizado: fondo azul degradado con una bola amarilla y detalles blancos representando el juego
- Splash screen personalizado y animado (la bola aparece con una animación de escala), integrado con la API oficial `SplashScreen` de Android 12+
- Nombre de la app cambiado a **"Bola Inclinada"**

---

## Proceso de desarrollo

### 1. Punto de partida

Se partió de una versión inicial simple del juego, compuesta por:

- `BallView.kt`: una `View` con `Canvas` que dibujaba una bola roja fija y la movía según los valores del acelerómetro.
- `MainActivity.kt`: registraba el `SensorManager` y actualizaba la vista con `onSensorChanged`.

Sobre esa base se fueron agregando, de forma incremental, todos los requisitos del reto (básico e intermedio) y luego las mejoras de diseño.

### 2. Nivel básico

Se amplió `BallView` para recibir el color y el tamaño de la bola como parámetros del constructor, en vez de tenerlos fijos en el código:

```kotlin
class BallView(
    context: Context,
    private val colorBola: Int = Color.parseColor("#E53935"),
    private val radioBola: Float = 60f
) : View(context) {
```

Estos valores se seleccionan en el menú y se envían por `Intent`:

```kotlin
val intent = Intent(this, MainActivity::class.java)
intent.putExtra("BALL_COLOR", colorElegido)
intent.putExtra("BALL_SIZE", tamanoElegido)
startActivity(intent)
```

Las coordenadas X/Y y el texto de instrucciones se dibujan directamente sobre el `Canvas` en `onDraw`.

### 3. Nivel intermedio — obstáculos, puntaje y choques

Se agregó una lista dinámica de obstáculos:

```kotlin
data class Obstaculo(var x: Float, var y: Float, val radio: Float)
private val obstaculos = mutableListOf<Obstaculo>()
```

La cantidad de obstáculos no es fija: crece según el puntaje del jugador, hasta un máximo, para lograr dificultad progresiva:

```kotlin
private fun actualizarObstaculos() {
    if (width == 0 || height == 0) return
    val cantidadDeseada = (cantidadBase + puntaje / 10).coerceAtMost(cantidadMaxima)
    if (cantidadDeseada == cantidadObjetivo) return
    cantidadObjetivo = cantidadDeseada

    while (obstaculos.size < cantidadDeseada) {
        val x = Random.nextInt(...)
        val y = Random.nextInt(...)
        obstaculos.add(Obstaculo(x, y, radio))
    }
}
```

El puntaje aumenta automáticamente cada segundo mediante un `CountDownTimer`:

```kotlin
private val timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
    override fun onTick(millisUntilFinished: Long) {
        if (!juegoTerminado) {
            puntaje += 1
            invalidate()
        }
    }
    override fun onFinish() {}
}
```

La detección de choques se hace por distancia entre centros (colisión circular) para los obstáculos, y por límites de pantalla para los bordes:

```kotlin
val distancia = Math.sqrt((dx * dx + dy * dy).toDouble())
if (distancia < radioBola + obstaculo.radio) {
    vidas -= 1
    flashTimer = 10
    if (vidas <= 0) {
        juegoTerminado = true
        timer.cancel()
        onGameOver?.invoke(puntaje)
    }
}
```

Cuando se acaban las vidas, `MainActivity` escucha el callback `onGameOver` y muestra un diálogo con el puntaje final:

```kotlin
ballView.onGameOver = { puntajeFinal ->
    runOnUiThread { mostrarDialogoFinal(puntajeFinal) }
}
```

con dos botones: **Reintentar** (reinicia `MainActivity`) y **Menú** (vuelve a `MainMenuActivity`).

### 4. Pantalla de inicio (menú) con Jetpack Compose

Se creó `MainMenuActivity` con Compose para elegir color y tamaño de la bola, y mostrar las instrucciones antes de jugar. Se hizo **responsive** usando `BoxWithConstraints`, de forma que la tarjeta del menú ocupe el 92% del ancho en celulares pero nunca supere los 420dp en tablets:

```kotlin
BoxWithConstraints(modifier = Modifier.fillMaxSize()...) {
    val anchoPantalla = maxWidth
    val esPantallaAncha = anchoPantalla > 600.dp
    val anchoTarjeta = if (esPantallaAncha) 420.dp else anchoPantalla * 0.92f
    ...
}
```

Todo el contenido está dentro de un `verticalScroll` para que no se corte en pantallas pequeñas u orientación horizontal.

### 5. Rediseño visual del juego

Se reemplazó el fondo gris plano por un paisaje con degradado de cielo y una franja de pasto, se le dio efecto de volumen a la bola con un `RadialGradient`, y los obstáculos pasaron de ser círculos simples a **árboles** dibujados con un tronco y tres capas de copa superpuestas para dar sensación de follaje:

```kotlin
private fun dibujarArbol(canvas: Canvas, cx: Float, cy: Float, radio: Float) {
    canvas.drawRoundRect(...)          // tronco
    canvas.drawCircle(cx, copaY, radio, paintCopaOscura)          // copa base
    canvas.drawCircle(cx - radio * 0.5f, ..., paintCopaOscura)    // copa izquierda
    canvas.drawCircle(cx + radio * 0.5f, ..., paintCopaOscura)    // copa derecha
    canvas.drawCircle(cx - radio * 0.2f, ..., paintCopaClara)     // brillo superior
}
```

El puntaje, las vidas y las coordenadas pasaron de texto suelto a un panel semitransparente con esquinas redondeadas.

### 6. Ícono adaptativo personalizado

Se diseñaron los recursos del ícono adaptativo (Android 8+) representando el juego: fondo azul degradado y una bola amarilla con detalles blancos como primer plano.

```xml
<!-- res/mipmap-anydpi-v26/ic_launcher.xml -->
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
</adaptive-icon>
```

Para que el ícono se actualice correctamente en el dispositivo/emulador (Android cachea el ícono por app), es necesario **desinstalar la app antes de reinstalarla**, o regenerar los recursos con `File > New > Image Asset` en Android Studio.

### 7. Splash screen personalizado

Desde Android 12, el sistema operativo dibuja automáticamente un splash con el ícono genérico de Android antes de que el código de la actividad se ejecute. Para personalizarlo se integró la API oficial `SplashScreen`:

```gradle
implementation("androidx.core:core-splashscreen:1.0.1")
```

```xml
<!-- res/values/themes.xml -->
<style name="Theme.App.Starting" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">#1565C0</item>
    <item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
    <item name="postSplashScreenTheme">@style/Theme.MyApplication</item>
</style>
```

```kotlin
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent { SplashScreen() }
        ...
    }
}
```

De esta forma, el splash del sistema usa el color y el ícono del juego, y a continuación se muestra la animación propia hecha en Compose (la bola aparece con una animación de escala) antes de pasar al menú.

---

## Estructura del proyecto

```
app/src/main/
├── java/com/example/myapplication/
│   ├── SplashActivity.kt      → Splash animado + integración con SplashScreen API
│   ├── MainMenuActivity.kt    → Menú responsive (color, tamaño, instrucciones)
│   ├── MainActivity.kt        → Sensores + diálogo de fin de juego
│   └── BallView.kt            → Lógica y dibujo del juego (Canvas)
├── res/
│   ├── drawable/
│   │   ├── ic_launcher_background.xml
│   │   └── ic_launcher_foreground.xml
│   ├── mipmap-anydpi-v26/
│   │   ├── ic_launcher.xml
│   │   └── ic_launcher_round.xml
│   └── values/
│       ├── strings.xml
│       └── themes.xml
└── AndroidManifest.xml
```

---

## Instalación y ejecución

1. Clona o descarga el proyecto y ábrelo en Android Studio.
2. Verifica que `app/build.gradle` tenga Compose habilitado:
   ```gradle
   buildFeatures {
       compose true
   }
   composeOptions {
       kotlinCompilerExtensionVersion = "1.5.x"
   }
   dependencies {
       implementation "androidx.compose.ui:ui:1.6.x"
       implementation "androidx.compose.material3:material3:1.2.x"
       implementation "androidx.activity:activity-compose:1.9.x"
       implementation "androidx.core:core-splashscreen:1.0.1"
   }
   ```
3. Sincroniza Gradle (`Sync Now`).
4. Ejecuta en un dispositivo físico o en un emulador con **sensores virtuales activados** (`Extended Controls > Virtual sensors`), ya que el juego depende del acelerómetro.
5. Si el ícono o el splash no se actualizan, desinstala la app del dispositivo/emulador antes de volver a correrla.

---

## Flujo de pantallas

`SplashActivity` (2.2s animado, con splash del sistema personalizado) → `MainMenuActivity` (elige color/tamaño, lee instrucciones, presiona "Iniciar Juego") → `MainActivity` (juego con `BallView`: mueve la bola inclinando el celular, esquiva árboles, evita los bordes, acumula puntaje, la dificultad sube con el puntaje) → al perder las 3 vidas, diálogo con puntaje final y opciones **Reintentar** / **Menú**.

---

## Capturas de la funcionalidad

| Splash Screen | Menú de inicio |
| :-----------: | :------------: |
| <img width="181" height="390" alt="image" src="https://github.com/user-attachments/assets/46dab438-0937-4b15-ae15-e90de1450119" />| <img width="222" height="487" alt="image" src="https://github.com/user-attachments/assets/0576d2e6-7f8b-4e88-a292-268b40487202" />|

| Juego en curso | Choque con obstáculo |
| :-------------: | :-------------------: |
| <img width="341" height="752" alt="image" src="https://github.com/user-attachments/assets/9eb48c01-12f5-4118-a2ce-4ad028094945" />| <img width="342" height="719" alt="image" src="https://github.com/user-attachments/assets/0c95c23a-5933-4578-92dd-d408974801fd" />|

| Choque con borde | Pantalla de Game Over |
| :---------------: | :--------------------: |
| <img width="337" height="727" alt="image" src="https://github.com/user-attachments/assets/31097d81-00f1-448f-a405-0a2365aedbfe" />| <img width="331" height="731" alt="image" src="https://github.com/user-attachments/assets/739b8355-4b07-4352-a38b-13ced53f4391" />|

---

## Resultados

- Se implementó la selección de color y tamaño de la bola desde un menú previo al juego.
- Se mostraron instrucciones y coordenadas X/Y en tiempo real sobre el `Canvas`.
- Se generaron obstáculos dinámicos con forma de árbol en posiciones aleatorias.
- Se implementó dificultad progresiva: la cantidad de obstáculos aumenta con el puntaje.
- Se implementó un sistema de puntaje por tiempo sobrevivido y un sistema de vidas.
- Se detectaron correctamente los choques contra los bordes de la pantalla y contra los obstáculos.
- Se construyó una pantalla de Game Over con opciones de reintentar o volver al menú.
- Se diseñó una pantalla de inicio responsive, adaptable a distintos tamaños de pantalla.
- Se personalizó por completo la identidad visual: ícono adaptativo y splash screen animado con la API oficial de Android.
