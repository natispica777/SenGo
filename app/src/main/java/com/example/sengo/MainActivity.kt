package com.example.sengo

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import kotlin.math.sqrt

// COLORES DE SENGO

val Fondo = Color(0xFFF5F7FA)
val Tarjeta = Color(0xFFFFFFFF)
val Naranja = Color(0xFFFF8738)
val Blanco = Color(0xFF20242C)
val Gris = Color(0xFF747B86)
val Verde = Color(0xFF35A875)

// RUTAS DE NAVEGACION

data object Home : NavKey
data object TrainingRoute : NavKey

// ACTIVIDAD PRINCIPAL

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SensorGoApp()
        }
    }
}

// NAVEGACION PRINCIPAL

@Composable
fun SensorGoApp() {

    val backStack = remember {
        NavBackStack<NavKey>(Home)
    }

    var selectedExercise by remember {
        mutableStateOf("Sentadillas")
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Naranja,
            background = Fondo,
            surface = Tarjeta
        )
    ) {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Fondo
        ) {

            NavDisplay(
                backStack = backStack,

                onBack = {
                    if (backStack.size > 1) {
                        backStack.removeLastOrNull()
                    }
                },

                entryProvider = { key ->

                    when (key) {

                        Home -> NavEntry(key) {
                            HomeScreen(
                                onExercise = { exercise ->
                                    selectedExercise = exercise
                                    backStack.add(TrainingRoute)
                                }
                            )
                        }

                        TrainingRoute -> NavEntry(key) {
                            TrainingScreen(
                                exercise = selectedExercise
                            )
                        }

                        else -> error("Pantalla desconocida")
                    }
                }
            )
        }
    }
}

// PANTALLA PRINCIPAL

@Composable
fun HomeScreen(
    onExercise: (String) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(35.dp))

        Text(
            text = "SENGO",
            color = Naranja,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Tu entrenador personal",
            color = Gris,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "¡Hora de entrenar!",
            color = Blanco,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Selecciona el ejercicio que deseas realizar",
            color = Gris,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(25.dp))

        MenuCard(
            emoji = "🏋️",
            titulo = "Sentadillas",
            descripcion = "Fortalece piernas y glúteos",
            onClick = { onExercise("Sentadillas") }
        )

        MenuCard(
            emoji = "💪",
            titulo = "Abdominales",
            descripcion = "Entrena tu zona abdominal",
            onClick = { onExercise("Abdominales") }
        )

        MenuCard(
            emoji = "🤸",
            titulo = "Saltos",
            descripcion = "Activa todo tu cuerpo",
            onClick = { onExercise("Saltos") }
        )

        Spacer(modifier = Modifier.height(30.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Tarjeta
            ),
            shape = RoundedCornerShape(18.dp)
        ) {

            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "📱 CONTEO AUTOMÁTICO",
                    color = Naranja,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "SenGo utiliza los sensores de movimiento de tu celular para contar tus repeticiones.",
                    color = Gris,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "SENGO • Muévete a tu ritmo",
            color = Gris,
            fontSize = 12.sp
        )
    }
}

// TARJETAS DE EJERCICIOS

@Composable
fun MenuCard(
    emoji: String,
    titulo: String,
    descripcion: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),

        shape = RoundedCornerShape(18.dp),

        colors = CardDefaults.cardColors(
            containerColor = Tarjeta
        ),

        onClick = onClick
    ) {

        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = emoji,
                fontSize = 35.sp
            )

            Spacer(modifier = Modifier.width(18.dp))

            Column {

                Text(
                    text = titulo,
                    color = Naranja,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = descripcion,
                    color = Gris,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// PANTALLA DE ENTRENAMIENTO

@Composable
fun TrainingScreen(exercise: String) {

    val context = LocalContext.current

    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE)
                as SensorManager
    }

    val accelerometer = remember {
        sensorManager.getDefaultSensor(
            Sensor.TYPE_ACCELEROMETER
        )
    }

    var repetitions by remember {
        mutableIntStateOf(0)
    }

    var isActive by remember {
        mutableStateOf(false)
    }

    var movementStatus by remember {
        mutableStateOf("Preparado para entrenar")
    }

    // DETECCION AUTOMATICA DE MOVIMIENTO

    DisposableEffect(isActive, exercise) {

        if (!isActive || accelerometer == null) {

            onDispose { }

        } else {

            var gravityX = 0f
            var gravityY = 0f
            var gravityZ = 0f

            var initialized = false
            var wasAboveThreshold = false
            var lastRepTime = 0L

            val threshold = when (exercise) {
                "Saltos" -> 2.0f
                "Sentadillas" -> 1.5f
                else -> 1.3f
            }

            val listener = object : SensorEventListener {

                override fun onSensorChanged(event: SensorEvent?) {

                    event ?: return

                    val xRaw = event.values[0]
                    val yRaw = event.values[1]
                    val zRaw = event.values[2]

                    // CALCULAR GRAVEDAD

                    if (!initialized) {

                        gravityX = xRaw
                        gravityY = yRaw
                        gravityZ = zRaw

                        initialized = true
                        return
                    }

                    val alpha = 0.8f

                    gravityX = alpha * gravityX +
                            (1 - alpha) * xRaw

                    gravityY = alpha * gravityY +
                            (1 - alpha) * yRaw

                    gravityZ = alpha * gravityZ +
                            (1 - alpha) * zRaw

                    // ACELERACION DEL MOVIMIENTO

                    val x = xRaw - gravityX
                    val y = yRaw - gravityY
                    val z = zRaw - gravityZ

                    val movement = sqrt(
                        x * x + y * y + z * z
                    )

                    val currentTime = System.currentTimeMillis()

                    movementStatus =
                        if (movement > 1.0f) {
                            "¡Movimiento detectado!"
                        } else {
                            "Esperando movimiento..."
                        }

                    // CONTADOR DE REPETICIONES

                    val aboveThreshold = movement > threshold

                    if (
                        aboveThreshold &&
                        !wasAboveThreshold &&
                        currentTime - lastRepTime > 850
                    ) {

                        repetitions++
                        lastRepTime = currentTime
                    }

                    wasAboveThreshold = aboveThreshold
                }

                override fun onAccuracyChanged(
                    sensor: Sensor?,
                    accuracy: Int
                ) {}
            }

            sensorManager.registerListener(
                listener,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME
            )

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    // INTERFAZ DEL ENTRENAMIENTO

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        ScreenTitle("ENTRENAMIENTO")

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "EJERCICIO SELECCIONADO",
            color = Gris,
            fontSize = 13.sp
        )

        Text(
            text = exercise,
            color = Naranja,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(35.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Tarjeta
            ),
            shape = RoundedCornerShape(22.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(25.dp),

                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "REPETICIONES",
                    color = Gris,
                    fontSize = 16.sp
                )

                Text(
                    text = "$repetitions",
                    color = Naranja,
                    fontSize = 85.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Movimientos registrados",
                    color = Gris,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Tarjeta
            ),
            shape = RoundedCornerShape(18.dp)
        ) {

            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "📱 SENSOR DE MOVIMIENTO",
                    color = Naranja,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = movementStatus,
                    color = Blanco,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isActive)
                        "Entrenamiento activo"
                    else
                        "Entrenamiento detenido",
                    color = if (isActive) Verde else Gris,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        Button(
            onClick = {
                isActive = !isActive
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = if (isActive) Verde else Naranja
            ),

            shape = RoundedCornerShape(14.dp)
        ) {

            Text(
                text = if (isActive)
                    "PAUSAR ENTRENAMIENTO"
                else
                    "INICIAR ENTRENAMIENTO",

                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                repetitions = 0
                isActive = false
                movementStatus = "Preparado para entrenar"
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),

            shape = RoundedCornerShape(14.dp)
        ) {

            Text(
                text = "REINICIAR ENTRENAMIENTO",
                color = Blanco
            )
        }

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "Consejo: coloca el celular de forma segura y realiza los movimientos con cuidado.",
            color = Gris,
            fontSize = 13.sp
        )
    }
}

// TITULO REUTILIZABLE

@Composable
fun ScreenTitle(titulo: String) {

    Text(
        text = titulo,
        color = Naranja,
        fontSize = 25.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    )
}
