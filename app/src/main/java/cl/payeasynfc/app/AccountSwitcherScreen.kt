
package cl.payeasynfc.app

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import java.util.concurrent.Executors

private val SwitcherBlue = Color(0xFF173B65)
private val SwitcherBackground = Color(0xFFF3F6FA)
private val SwitcherGreen = Color(0xFF21865B)

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
            .background(SwitcherBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "PayEasy NFC",
            style = MaterialTheme.typography.titleLarge,
            color = SwitcherBlue,
            fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Cambiar de cuenta",
                style = MaterialTheme.typography.headlineMedium,
                color = SwitcherBlue,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Selecciona otra cuenta registrada en este dispositivo.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF65758B)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "CUENTA ACTIVA",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF718096),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentEmail,
                    style = MaterialTheme.typography.titleMedium,
                    color = SwitcherBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Cuentas registradas",
                style = MaterialTheme.typography.titleMedium,
                color = SwitcherBlue,
                fontWeight = FontWeight.Bold
            )

            when {
                loading -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(color = SwitcherBlue)
                            Text("Cargando cuentas...")
                        }
                    }
                }

                accounts.isEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Text(
                            text = "No hay cuentas registradas en este dispositivo.",
                            modifier = Modifier.padding(18.dp),
                            color = Color(0xFF65758B)
                        )
                    }
                }

                else -> {
                    accounts.forEach { account ->
                        val isCurrent = account.email == currentEmail
                        val isSelected = selectedAccount?.email == account.email

                        AccountOptionCard(
                            name = account.name,
                            email = account.email,
                            isCurrent = isCurrent,
                            isSelected = isSelected,
                            onClick = {
                                selectedAccount = account
                                password = ""
                                message = null
                            }
                        )
                    }
                }
            }
        }

        selectedAccount?.let { account ->
            HorizontalDivider(color = Color(0xFFDCE3EC))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Confirmar cambio",
                        style = MaterialTheme.typography.titleLarge,
                        color = SwitcherBlue,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Para cambiar a esta cuenta, confirma su contraseña.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF65758B)
                    )

                    Text(
                        text = account.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SwitcherBlue,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            message = null
                        },
                        label = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !verifying,
                        shape = RoundedCornerShape(12.dp),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SwitcherBlue
                        )
                    ) {
                        if (verifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                            Text("Verificando...")
                        } else {
                            Text("Confirmar cambio de cuenta")
                        }
                    }

                    message?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Volver al perfil")
        }

        Text(
            text = "PayEasy NFC · Gestión de cuentas",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF8995A5),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun AccountOptionCard(
    name: String,
    email: String,
    isCurrent: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) SwitcherBlue else Color(0xFFE1E7EF)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color(0xFFEAF1F9)
            } else {
                Color.White
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .background(Color.Transparent, CircleShape),
                shape = CircleShape,
                color = Color(0xFFEAF1F9)
            ) {
                Text(
                    text = name.firstOrNull()?.uppercase() ?: "U",
                    modifier = Modifier.padding(12.dp),
                    color = SwitcherBlue,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = name,
                    color = SwitcherBlue,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = email,
                    color = Color(0xFF65758B),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (isCurrent) {
                    Text(
                        text = "Cuenta actual",
                        color = SwitcherGreen,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isSelected) {
                Text(
                    text = "✓",
                    color = SwitcherBlue,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountOptionCardPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SwitcherBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AccountOptionCard(
            name = "Comercio de ejemplo",
            email = "comercio@ejemplo.cl",
            isCurrent = true,
            isSelected = false,
            onClick = {}
        )

        AccountOptionCard(
            name = "Otra cuenta",
            email = "otra@ejemplo.cl",
            isCurrent = false,
            isSelected = true,
            onClick = {}
        )
    }
}
