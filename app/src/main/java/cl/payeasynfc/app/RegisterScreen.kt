package cl.payeasynfc.app

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import java.util.concurrent.Executors

@Composable
fun RegisterScreen() {
    val context = LocalContext.current
    val database = remember { SellerDatabase(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "PayEasy NFC",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Crear cuenta de vendedor",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del vendedor") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            )
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmarPassword,
            onValueChange = { confirmarPassword = it },
            label = { Text("Confirmar contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                mensaje = ""

                if (
                    nombre.isBlank() ||
                    correo.isBlank() ||
                    password.isBlank() ||
                    confirmarPassword.isBlank()
                ) {
                    mensaje = "Completa todos los campos."
                    return@Button
                }

                if (password != confirmarPassword) {
                    mensaje = "Las contraseñas no coinciden."
                    return@Button
                }

                guardando = true

                executor.execute {
                    val resultado = try {
                        database.registerSeller(
                            nombre.trim(),
                            correo.trim(),
                            password
                        )
                    } catch (e: Exception) {
                        false
                    }

                    mainHandler.post {
                        guardando = false

                        if (resultado) {
                            mensaje = "¡Cuenta creada correctamente!"
                            nombre = ""
                            correo = ""
                            password = ""
                            confirmarPassword = ""
                        } else {
                            mensaje =
                                "No se pudo crear la cuenta. " +
                                        "Comprueba si el correo ya está registrado."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !guardando
        ) {
            Text(if (guardando) "Guardando..." else "Crear cuenta")
        }

        if (mensaje.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = mensaje,
                color = if (mensaje.startsWith("¡")) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}