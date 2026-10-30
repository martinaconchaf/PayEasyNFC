package cl.payeasynfc.app

import android.os.Handler
import android.os.Looper
import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import java.util.concurrent.Executors

@Composable
fun RegisterScreen(
    onGoToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val database = remember { SellerDatabase(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }

    var errorNombre by remember { mutableStateOf("") }
    var errorCorreo by remember { mutableStateOf("") }
    var errorPassword by remember { mutableStateOf("") }
    var errorConfirmacion by remember { mutableStateOf("") }

    var mensaje by remember { mutableStateOf("") }
    var registroExitoso by remember { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
            text = "Crear cuenta de vendedor",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                errorNombre = ""
                mensaje = ""
            },
            label = { Text("Nombre del vendedor") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorNombre.isNotEmpty(),
            supportingText = {
                if (errorNombre.isNotEmpty()) {
                    Text(errorNombre)
                }
            }
        )

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
                errorCorreo = ""
                mensaje = ""
            },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorCorreo.isNotEmpty(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            supportingText = {
                if (errorCorreo.isNotEmpty()) {
                    Text(errorCorreo)
                }
            }
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorPassword = ""
                mensaje = ""
            },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorPassword.isNotEmpty(),
            visualTransformation = PasswordVisualTransformation(),
            supportingText = {
                if (errorPassword.isNotEmpty()) {
                    Text(errorPassword)
                } else {
                    Text("Debe contener al menos 8 caracteres.")
                }
            }
        )

        OutlinedTextField(
            value = confirmarPassword,
            onValueChange = {
                confirmarPassword = it
                errorConfirmacion = ""
                mensaje = ""
            },
            label = { Text("Confirmar contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorConfirmacion.isNotEmpty(),
            visualTransformation = PasswordVisualTransformation(),
            supportingText = {
                if (errorConfirmacion.isNotEmpty()) {
                    Text(errorConfirmacion)
                }
            }
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                // Limpiar errores anteriores
                errorNombre = ""
                errorCorreo = ""
                errorPassword = ""
                errorConfirmacion = ""
                mensaje = ""

                // Validar nombre
                if (nombre.trim().length < 2) {
                    errorNombre =
                        "Ingresa un nombre de al menos 2 caracteres."
                }

                // Validar correo
                if (correo.isBlank()) {
                    errorCorreo = "El correo es obligatorio."
                } else if (
                    !Patterns.EMAIL_ADDRESS
                        .matcher(correo.trim())
                        .matches()
                ) {
                    errorCorreo = "Ingresa un correo válido."
                }

                // Validar contraseña
                if (password.isBlank()) {
                    errorPassword = "La contraseña es obligatoria."
                } else if (password.length < 8) {
                    errorPassword =
                        "La contraseña debe tener al menos 8 caracteres."
                }

                // Validar confirmación
                if (confirmarPassword.isBlank()) {
                    errorConfirmacion =
                        "Confirma tu contraseña."
                } else if (password != confirmarPassword) {
                    errorConfirmacion =
                        "Las contraseñas no coinciden."
                }

                // No guardar si hay errores
                val formularioValido =
                    errorNombre.isEmpty() &&
                            errorCorreo.isEmpty() &&
                            errorPassword.isEmpty() &&
                            errorConfirmacion.isEmpty()

                if (!formularioValido) {
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
                            registroExitoso = true
                            mensaje = "¡Cuenta creada correctamente!"

                            nombre = ""
                            correo = ""
                            password = ""
                            confirmarPassword = ""
                        } else {
                            registroExitoso = false
                            errorCorreo =
                                "No se pudo registrar. Comprueba si el correo ya existe."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !guardando
        ) {
            Text(
                if (guardando) "Validando y guardando..."
                else "Crear cuenta"
            )
        }

        Spacer(Modifier.height(16.dp))

        TextButton(
            onClick = onGoToLogin
        ) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }

        if (mensaje.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = mensaje,
                color = if (registroExitoso) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            database.close()
        }
    }
}