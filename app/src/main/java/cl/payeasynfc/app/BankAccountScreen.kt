package cl.payeasynfc.app

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.concurrent.Executors

@Composable
fun BankAccountScreen(
    sellerEmail: String,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { SellerDatabase(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val handler = remember { Handler(Looper.getMainLooper()) }

    var banco by remember { mutableStateOf("") }
    var tipoCuenta by remember { mutableStateOf("Cuenta corriente") }
    var titular by remember { mutableStateOf("") }
    var numeroCuenta by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PayEasy NFC",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Asociar cuenta bancaria",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Vendedor: $sellerEmail",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = banco,
            onValueChange = {
                banco = it
                mensaje = ""
            },
            label = { Text("Nombre del banco") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Tipo de cuenta",
            modifier = Modifier.fillMaxWidth()
        )

        var menuAbierto by remember { mutableStateOf(false) }

        Box {
            OutlinedButton(
                onClick = { menuAbierto = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(tipoCuenta)
            }

            DropdownMenu(
                expanded = menuAbierto,
                onDismissRequest = { menuAbierto = false }
            ) {
                listOf(
                    "Cuenta corriente",
                    "Cuenta vista",
                    "Cuenta de ahorro"
                ).forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(tipo) },
                        onClick = {
                            tipoCuenta = tipo
                            menuAbierto = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = titular,
            onValueChange = { titular = it },
            label = { Text("Titular de la cuenta") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = numeroCuenta,
            onValueChange = { numeroCuenta = it },
            label = { Text("Número de cuenta") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (
                    banco.isBlank() ||
                    titular.isBlank() ||
                    numeroCuenta.isBlank()
                ) {
                    mensaje = "Completa todos los campos."
                    return@Button
                }

                guardando = true

                executor.execute {
                    val resultado = try {
                        val sellerId =
                            database.getSellerIdByEmail(sellerEmail)

                        sellerId != null &&
                                database.saveBankAccount(
                                    sellerId = sellerId,
                                    bankName = banco.trim(),
                                    accountType = tipoCuenta,
                                    accountHolder = titular.trim(),
                                    accountNumber = numeroCuenta.trim()
                                )
                    } catch (e: Exception) {
                        false
                    }

                    handler.post {
                        guardando = false

                        mensaje = if (resultado) {
                            "¡Cuenta bancaria asociada correctamente!"
                        } else {
                            "No se pudo guardar la cuenta bancaria."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !guardando
        ) {
            Text(
                if (guardando) "Guardando..."
                else "Asociar cuenta"
            )
        }

        if (mensaje.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = mensaje,
                color = if (
                    mensaje.startsWith("¡")
                ) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onBackToLogin) {
            Text("Volver al inicio de sesión")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            database.close()
        }
    }
}