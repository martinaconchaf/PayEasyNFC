package cl.payeasynfc.app

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import java.util.concurrent.Executors

@Composable
fun AccountSwitcherScreen(
    currentEmail: String,
    onAccountChanged: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { SellerDatabase(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val handler = remember { Handler(Looper.getMainLooper()) }

    var accounts by remember {
        mutableStateOf<List<SellerAccountOption>>(emptyList())
    }
    var selectedAccount by remember {
        mutableStateOf<SellerAccountOption?>(null)
    }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var verifying by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            database.close()
        }
    }

    LaunchedEffect(Unit) {
        executor.execute {
            val result = database.getRegisteredSellers()

            handler.post {
                accounts = result
                loading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Cambiar de cuenta",
            style = MaterialTheme.typography.headlineSmall
        )

        Text("Cuenta activa: $currentEmail")

        Text(
            text = "Cuentas registradas en este dispositivo",
            style = MaterialTheme.typography.titleMedium
        )

        if (loading) {
            CircularProgressIndicator()
        } else if (accounts.isEmpty()) {
            Text("No hay cuentas registradas.")
        } else {
            accounts.forEach { account ->
                OutlinedButton(
                    onClick = {
                        selectedAccount = account
                        password = ""
                        message = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(account.name)
                        Text(
                            text = account.email,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (account.email == currentEmail) {
                            Text(
                                "Cuenta actual",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        selectedAccount?.let { account ->
            HorizontalDivider()

            Text(
                text = "Autenticar cuenta seleccionada",
                style = MaterialTheme.typography.titleMedium
            )

            Text("Cuenta: ${account.email}")

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    message = null
                },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    message = null

                    if (password.isBlank()) {
                        message = "Ingresa la contraseña de la cuenta"
                    } else {
                        verifying = true
                        val enteredPassword = password

                        executor.execute {
                            val result = database.authenticateSeller(
                                account.email,
                                enteredPassword
                            )

                            handler.post {
                                verifying = false

                                when (result) {
                                    LoginResult.SUCCESS -> {
                                        onAccountChanged(account.email)
                                    }

                                    LoginResult.INVALID_CREDENTIALS -> {
                                        password = ""
                                        message = "Contraseña incorrecta"
                                    }

                                    LoginResult.ACCOUNT_LOCKED -> {
                                        password = ""
                                        message =
                                            "La cuenta está bloqueada temporalmente"
                                    }
                                }
                            }
                        }
                    }
                },
                enabled = !verifying,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (verifying) "Verificando..."
                    else "Confirmar cambio de cuenta"
                )
            }
        }

        message?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al perfil")
        }
    }
}