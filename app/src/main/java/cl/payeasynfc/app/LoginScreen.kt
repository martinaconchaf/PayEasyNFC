
package cl.payeasynfc.app

import android.os.Handler
import android.os.Looper
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import java.util.concurrent.Executors

private val PayEasyBlue = Color(0xFF173B65)
private val PayEasyBackground = Color(0xFFF3F6FA)

@Composable
fun LoginScreen(
    onBackToRegister: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val database = remember { SellerDatabase(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var accesoExitoso by remember { mutableStateOf(false) }
    var ingresando by remember { mutableStateOf(false) }

    LoginContent(
        correo = correo,
        password = password,
        mensaje = mensaje,
        accesoExitoso = accesoExitoso,
        ingresando = ingresando,
        onCorreoChange = {
            correo = it
            mensaje = ""
        },
        onPasswordChange = {
            password = it
            mensaje = ""
        },
        onBackToRegister = onBackToRegister,
        onLoginClick = {
            mensaje = ""
            accesoExitoso = false

            if (correo.isBlank() || password.isBlank()) {
                mensaje = "Ingresa tu correo y contraseña."
                return@LoginContent
            }

            ingresando = true

            executor.execute {
                val resultado = try {
                    database.authenticateSeller(correo, password)
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
                            mensaje = "Correo o contraseña incorrectos."
                        }

                        LoginResult.ACCOUNT_LOCKED -> {
                            mensaje =
                                "Cuenta bloqueada temporalmente. Intenta nuevamente en 15 minutos."
                        }

                        null -> {
                            mensaje = "Ocurrió un error. Intenta nuevamente."
                        }
                    }
                }
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            database.close()
        }
    }
}

@Composable
private fun LoginContent(
    correo: String,
    password: String,
    mensaje: String,
    accesoExitoso: Boolean,
    ingresando: Boolean,
    onCorreoChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onBackToRegister: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PayEasyBackground)
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
            color = PayEasyBlue
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pagos simples, rápidos y seguros",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

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
                    text = "Bienvenido",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = PayEasyBlue
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Inicia sesión en tu cuenta",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(28.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = onCorreoChange,
                    label = { Text("Correo electrónico") },
                    placeholder = { Text("ejemplo@correo.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = !ingresando,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PayEasyBlue
                    )
                ) {
                    Text(
                        text = if (ingresando) {
                            "Verificando..."
                        } else {
                            "Ingresar"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (mensaje.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = mensaje,
                        color = if (accesoExitoso) {
                            Color(0xFF24834A)
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(
            onClick = onBackToRegister
        ) {
            Text(
                text = "¿No tienes cuenta? Regístrate",
                color = PayEasyBlue,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    MaterialTheme {
        LoginContent(
            correo = "",
            password = "",
            mensaje = "",
            accesoExitoso = false,
            ingresando = false,
            onCorreoChange = {},
            onPasswordChange = {},
            onBackToRegister = {},
            onLoginClick = {}
        )
    }
}
