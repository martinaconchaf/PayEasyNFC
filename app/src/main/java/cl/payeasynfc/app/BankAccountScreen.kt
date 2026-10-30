package cl.payeasynfc.app

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
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
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    var bankName by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf("") }
    var accountHolder by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }

    var bankError by remember { mutableStateOf<String?>(null) }
    var typeError by remember { mutableStateOf<String?>(null) }
    var holderError by remember { mutableStateOf<String?>(null) }
    var numberError by remember { mutableStateOf<String?>(null) }

    var menuExpanded by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            database.close()
        }
    }

    fun validate(): Boolean {
        bankError = if (bankName.isBlank()) {
            "Ingresa el nombre del banco"
        } else null

        typeError = if (accountType !in listOf(
                "Cuenta corriente",
                "Cuenta vista",
                "Cuenta de ahorro"
            )
        ) {
            "Selecciona un tipo de cuenta"
        } else null

        holderError = if (accountHolder.trim().length < 2) {
            "Ingresa el nombre del titular (mínimo 2 caracteres)"
        } else null

        numberError = if (!accountNumber.matches(Regex("^[0-9]{6,20}$"))) {
            "Ingresa entre 6 y 20 dígitos, sin espacios ni letras"
        } else null

        return bankError == null &&
                typeError == null &&
                holderError == null &&
                numberError == null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Asociar cuenta bancaria",
            style = MaterialTheme.typography.headlineSmall
        )

        Text("Vendedor: $sellerEmail")

        OutlinedTextField(
            value = bankName,
            onValueChange = {
                bankName = it
                bankError = null
            },
            label = { Text("Nombre del banco") },
            isError = bankError != null,
            supportingText = {
                bankError?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Column {
            OutlinedTextField(
                value = accountType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de cuenta") },
                isError = typeError != null,
                supportingText = {
                    typeError?.let { Text(it) }
                },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = {
                        menuExpanded = true
                    }) {
                        Text("Elegir")
                    }
                }
            )

            Box {
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    listOf(
                        "Cuenta corriente",
                        "Cuenta vista",
                        "Cuenta de ahorro"
                    ).forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                accountType = option
                                typeError = null
                                menuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = accountHolder,
            onValueChange = {
                accountHolder = it
                holderError = null
            },
            label = { Text("Nombre del titular") },
            isError = holderError != null,
            supportingText = {
                holderError?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = accountNumber,
            onValueChange = {
                accountNumber = it.filter { character ->
                    character.isDigit()
                }
                numberError = null
            },
            label = { Text("Número de cuenta") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            isError = numberError != null,
            supportingText = {
                numberError?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                message = null

                if (validate()) {
                    saving = true

                    executor.execute {
                        val sellerId =
                            database.getSellerIdByEmail(sellerEmail)

                        val saved = sellerId != null &&
                                database.saveBankAccount(
                                    sellerId = sellerId,
                                    bankName = bankName.trim(),
                                    accountType = accountType,
                                    accountHolder = accountHolder.trim(),
                                    accountNumber = accountNumber
                                )

                        mainHandler.post {
                            saving = false
                            message = if (saved) {
                                "Cuenta bancaria guardada correctamente"
                            } else {
                                "No se pudo guardar la cuenta bancaria"
                            }
                        }
                    }
                }
            },
            enabled = !saving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (saving) "Guardando..." else "Guardar cuenta")
        }

        message?.let {
            Text(
                text = it,
                color = if (it.startsWith("Cuenta bancaria")) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }

        OutlinedButton(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}