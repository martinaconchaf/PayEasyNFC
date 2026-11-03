
package cl.payeasynfc.app

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.concurrent.Executors

private val BankScreenBlue = Color(0xFF173B65)
private val BankScreenBackground = Color(0xFFF3F6FA)
private val BankScreenGreen = Color(0xFF23865B)

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
    var loadingAccount by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            database.close()
        }
    }

    LaunchedEffect(sellerEmail) {
        loadingAccount = true

        executor.execute {
            val sellerId = database.getSellerIdByEmail(sellerEmail)

            val existingAccount = sellerId?.let {
                database.getBankAccountBySellerId(it)
            }

            mainHandler.post {
                if (existingAccount != null) {
                    bankName = existingAccount.bankName
                    accountType = existingAccount.accountType
                    accountHolder = existingAccount.accountHolder
                    accountNumber = existingAccount.accountNumber

                    message = "Se cargó la cuenta bancaria registrada."
                } else {
                    message = "Este vendedor todavía no tiene una cuenta bancaria."
                }

                loadingAccount = false
            }
        }
    }

    fun validate(): Boolean {
        bankError = if (bankName.isBlank()) {
            "Ingresa el nombre del banco"
        } else null

        typeError = if (
            accountType !in listOf(
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

    BankAccountContent(
        sellerEmail = sellerEmail,
        bankName = bankName,
        onBankNameChange = {
            bankName = it
            bankError = null
        },
        bankError = bankError,
        accountType = accountType,
        onAccountTypeClick = {
            menuExpanded = true
        },
        menuExpanded = menuExpanded,
        onMenuDismiss = {
            menuExpanded = false
        },
        onAccountTypeSelected = {
            accountType = it
            typeError = null
            menuExpanded = false
        },
        typeError = typeError,
        accountHolder = accountHolder,
        onAccountHolderChange = {
            accountHolder = it
            holderError = null
        },
        holderError = holderError,
        accountNumber = accountNumber,
        onAccountNumberChange = {
            accountNumber = it.filter { character -> character.isDigit() }
            numberError = null
        },
        numberError = numberError,
        message = message,
        saving = saving,
        loadingAccount = loadingAccount,
        onSave = {
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
        onBackToLogin = onBackToLogin
    )
}

@Composable
private fun BankAccountContent(
    sellerEmail: String,
    bankName: String,
    onBankNameChange: (String) -> Unit,
    bankError: String?,
    accountType: String,
    onAccountTypeClick: () -> Unit,
    menuExpanded: Boolean,
    onMenuDismiss: () -> Unit,
    onAccountTypeSelected: (String) -> Unit,
    typeError: String?,
    accountHolder: String,
    onAccountHolderChange: (String) -> Unit,
    holderError: String?,
    accountNumber: String,
    onAccountNumberChange: (String) -> Unit,
    numberError: String?,
    message: String?,
    saving: Boolean,
    loadingAccount: Boolean,
    onSave: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val accountTypes = listOf(
        "Cuenta corriente",
        "Cuenta vista",
        "Cuenta de ahorro"
    )

    val messageIsSuccess = message?.startsWith("Se cargó") == true ||
            message?.startsWith("Cuenta bancaria guardada") == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BankScreenBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "PayEasy NFC",
                color = BankScreenBlue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Administrar cuenta bancaria",
                color = BankScreenBlue,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Registra o actualiza los datos de la cuenta donde recibirás tus pagos.",
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Text(
                    text = "Datos del vendedor",
                    color = BankScreenBlue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = sellerEmail,
                    color = Color(0xFF64748B),
                    style = MaterialTheme.typography.bodyMedium
                )

                HorizontalDivider(color = Color(0xFFE7ECF2))

                Text(
                    text = "Información bancaria",
                    color = BankScreenBlue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = bankName,
                    onValueChange = onBankNameChange,
                    label = { Text("Nombre del banco") },
                    placeholder = { Text("Ej. Banco de Chile") },
                    isError = bankError != null,
                    supportingText = {
                        bankError?.let { Text(it) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Column {
                    OutlinedTextField(
                        value = accountType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de cuenta") },
                        placeholder = { Text("Selecciona una opción") },
                        isError = typeError != null,
                        supportingText = {
                            typeError?.let { Text(it) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            TextButton(
                                onClick = onAccountTypeClick
                            ) {
                                Text("Elegir")
                            }
                        }
                    )

                    Box {
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = onMenuDismiss
                        ) {
                            accountTypes.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        onAccountTypeSelected(option)
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = accountHolder,
                    onValueChange = onAccountHolderChange,
                    label = { Text("Nombre del titular") },
                    placeholder = { Text("Nombre completo") },
                    isError = holderError != null,
                    supportingText = {
                        holderError?.let { Text(it) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = onAccountNumberChange,
                    label = { Text("Número de cuenta") },
                    placeholder = { Text("Ingresa solo números") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = numberError != null,
                    supportingText = {
                        numberError?.let { Text(it) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "El número de cuenta debe contener entre 6 y 20 dígitos.",
                    color = Color(0xFF64748B),
                    style = MaterialTheme.typography.bodySmall
                )

                if (loadingAccount) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = BankScreenBlue,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Cargando cuenta...")
                    }
                }

                message?.let {
                    Text(
                        text = it,
                        color = if (messageIsSuccess) {
                            BankScreenGreen
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = onSave,
                    enabled = !saving && !loadingAccount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BankScreenBlue
                    )
                ) {
                    if (saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Guardando...")
                    } else {
                        Text(
                            text = "Guardar cuenta",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onBackToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Volver")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BankAccountScreenPreview() {
    MaterialTheme {
        BankAccountContent(
            sellerEmail = "vendedor@ejemplo.cl",
            bankName = "Banco de Chile",
            onBankNameChange = {},
            bankError = null,
            accountType = "Cuenta corriente",
            onAccountTypeClick = {},
            menuExpanded = false,
            onMenuDismiss = {},
            onAccountTypeSelected = {},
            typeError = null,
            accountHolder = "Juan Pérez",
            onAccountHolderChange = {},
            holderError = null,
            accountNumber = "1234567890",
            onAccountNumberChange = {},
            numberError = null,
            message = "Se cargó la cuenta bancaria registrada.",
            saving = false,
            loadingAccount = false,
            onSave = {},
            onBackToLogin = {}
        )
    }
}
