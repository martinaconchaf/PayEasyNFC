
package cl.payeasynfc.app

import android.os.Handler
import android.os.Looper
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.concurrent.Executors

private val RegisterPayEasyBlue = Color(0xFF173B65)
private val RegisterPayEasyBackground = Color(0xFFF3F6FA)

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

    RegisterContent(
        nombre = nombre,
        correo = correo,
        password = password,
        confirmarPassword = confirmarPassword,
        errorNombre = errorNombre,
        errorCorreo = errorCorreo,
        errorPassword = errorPassword,
        errorConfirmacion = errorConfirmacion,
        mensaje = mensaje,
        registroExitoso = registroExitoso,
        guardando = guardando,

        onNombreChange = {
            nombre = it
            errorNombre = ""
            mensaje = ""
        },
        onCorreoChange = {
            correo = it
            errorCorreo = ""
            mensaje = ""
        },
        onPasswordChange = {
            password = it
            errorPassword = ""
            mensaje = ""
        },
        onConfirmarPasswordChange = {
            confirmarPassword = it
            errorConfirmacion = ""
            mensaje = ""
        },

        onRegisterClick = {
            errorNombre = ""
            errorCorreo = ""
            errorPassword = ""
            errorConfirmacion = ""
            mensaje = ""
            registroExitoso = false

            if (nombre.trim().length < 2) {
                errorNombre =
                    "Ingresa un nombre de al menos 2 caracteres."
            }

            if (correo.isBlank()) {
                errorCorreo = "El correo es obligatorio."
            } else if (
                !Patterns.EMAIL_ADDRESS
                    .matcher(correo.trim())
                    .matches()
            ) {
                errorCorreo = "Ingresa un correo válido."
            }

            if (password.isBlank()) {
                errorPassword = "La contraseña es obligatoria."
            } else if (password.length < 8) {
                errorPassword =
                    "La contraseña debe tener al menos 8 caracteres."
            }

            if (confirmarPassword.isBlank()) {
                errorConfirmacion = "Confirma tu contraseña."
            } else if (password != confirmarPassword) {
                errorConfirmacion =
                    "Las contraseñas no coinciden."
            }

            val formularioValido =
                errorNombre.isEmpty() &&
                        errorCorreo.isEmpty() &&
                        errorPassword.isEmpty() &&
                        errorConfirmacion.isEmpty()

            if (!formularioValido) {
                return@RegisterContent
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

        onGoToLogin = onGoToLogin
    )

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            database.close()
        }
    }
}

@Composable
private fun RegisterContent(
    nombre: String,
    correo: String,
    password: String,
    confirmarPassword: String,
    errorNombre: String,
    errorCorreo: String,
    errorPassword: String,
    errorConfirmacion: String,
    mensaje: String,
    registroExitoso: Boolean,
    guardando: Boolean,
    onNombreChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmarPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onGoToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RegisterPayEasyBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "PayEasy NFC",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = RegisterPayEasyBlue
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pagos simples, rápidos y seguros",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 5.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Crear cuenta",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = RegisterPayEasyBlue
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Registra tus datos para comenzar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = onNombreChange,
                    label = { Text("Nombre del vendedor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorNombre.isNotEmpty(),
                    shape = RoundedCornerShape(14.dp),
                    supportingText = {
                        if (errorNombre.isNotEmpty()) {
                            Text(errorNombre)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = onCorreoChange,
                    label = { Text("Correo electrónico") },
                    placeholder = { Text("ejemplo@correo.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorCorreo.isNotEmpty(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    supportingText = {
                        if (errorCorreo.isNotEmpty()) {
                            Text(errorCorreo)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorPassword.isNotEmpty(),
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation =
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    supportingText = {
                        if (errorPassword.isNotEmpty()) {
                            Text(errorPassword)
                        } else {
                            Text("Debe contener al menos 8 caracteres.")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmarPassword,
                    onValueChange = onConfirmarPasswordChange,
                    label = { Text("Confirmar contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorConfirmacion.isNotEmpty(),
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation =
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    supportingText = {
                        if (errorConfirmacion.isNotEmpty()) {
                            Text(errorConfirmacion)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = !guardando,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RegisterPayEasyBlue
                    )
                ) {
                    Text(
                        text = if (guardando) {
                            "Validando y guardando..."
                        } else {
                            "Crear cuenta"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (mensaje.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = mensaje,
                        color = if (registroExitoso) {
                            Color(0xFF24834A)
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        TextButton(
            onClick = onGoToLogin
        ) {
            Text(
                text = "¿Ya tienes cuenta? Inicia sesión",
                color = RegisterPayEasyBlue,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegisterScreenPreview() {
    MaterialTheme {
        RegisterContent(
            nombre = "",
            correo = "",
            password = "",
            confirmarPassword = "",
            errorNombre = "",
            errorCorreo = "",
            errorPassword = "",
            errorConfirmacion = "",
            mensaje = "",
            registroExitoso = false,
            guardando = false,
            onNombreChange = {},
            onCorreoChange = {},
            onPasswordChange = {},
            onConfirmarPasswordChange = {},
            onRegisterClick = {},
            onGoToLogin = {}
        )
    }
}
