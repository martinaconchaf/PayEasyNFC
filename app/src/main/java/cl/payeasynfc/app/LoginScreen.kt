package cl.payeasynfc.app

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import java.util.concurrent.Executors

@Composable
fun LoginScreen(
    onBackToRegister: () -> Unit,
    onLoginSuccess: (String) -> Unit
){
    val context = LocalContext.current
    val database = remember { SellerDatabase(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var accesoExitoso by remember { mutableStateOf(false) }
    var ingresando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "PayEasy NFC",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Iniciar sesión",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
                mensaje = ""
            },
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
            onValueChange = {
                password = it
                mensaje = ""
            },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                mensaje = ""
                accesoExitoso = false

                if (correo.isBlank() || password.isBlank()) {
                    mensaje = "Ingresa tu correo y contraseña."
                    return@Button
                }

                ingresando = true

                executor.execute {
                    val resultado = try {
                        database.authenticateSeller(
                            correo,
                            password
                        )
                    } catch (e: Exception) {
                        null
                    }

                    mainHandler.post {
                        ingresando = false

                        when (resultado) {
                            LoginResult.SUCCESS -> {
                                accesoExitoso = true
                                mensaje = "¡Inicio de sesión exitoso!"
                                onLoginSuccess(correo.trim().lowercase())
                            }

                            LoginResult.INVALID_CREDENTIALS -> {
                                mensaje =
                                    "Correo o contraseña incorrectos."
                            }

                            LoginResult.ACCOUNT_LOCKED -> {
                                mensaje =
                                    "Cuenta bloqueada temporalmente. " +
                                            "Intenta nuevamente en 15 minutos."
                            }

                            null -> {
                                mensaje =
                                    "Ocurrió un error. Intenta nuevamente."
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !ingresando
        ) {
            Text(
                if (ingresando) "Verificando..."
                else "Ingresar"
            )
        }

        if (mensaje.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = mensaje,
                color = if (accesoExitoso) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        TextButton(
            onClick = onBackToRegister
        ) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            database.close()
        }
    }
}