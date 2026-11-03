
package cl.payeasynfc.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

private val AmountBlue = Color(0xFF173B65)
private val AmountBackground = Color(0xFFF3F6FA)
private val AmountGreen = Color(0xFF21865B)
private val AmountText = Color(0xFF263445)

@Composable
fun SaleAmountScreen(
    onAmountConfirmed: (Long) -> Unit,
    onBack: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    SaleAmountContent(
        amount = amount,
        onAmountChange = {
            amount = it
            errorMessage = ""
        },
        errorMessage = errorMessage,
        onContinue = {
            val cleanAmount = amount.trim()

            when {
                cleanAmount.isEmpty() -> {
                    errorMessage = "Debes ingresar un monto."
                }

                !cleanAmount.all { it.isDigit() } -> {
                    errorMessage = "Ingresa solamente números enteros positivos."
                }

                else -> {
                    val numericAmount = cleanAmount.toLongOrNull()

                    when {
                        numericAmount == null -> {
                            errorMessage = "El monto ingresado no es válido."
                        }

                        numericAmount <= 0L -> {
                            errorMessage = "El monto debe ser mayor que cero."
                        }

                        else -> onAmountConfirmed(numericAmount)
                    }
                }
            }
        },
        onBack = onBack
    )
}

@Composable
private fun SaleAmountContent(
    amount: String,
    onAmountChange: (String) -> Unit,
    errorMessage: String,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmountBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "PayEasyNFC",
            style = MaterialTheme.typography.titleLarge,
            color = AmountBlue,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "Nuevo cobro",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AmountBlue,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Ingresa el monto de la venta que deseas cobrar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AmountText
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            AmountBackground,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "MONTO DE LA VENTA",
                            style = MaterialTheme.typography.labelMedium,
                            color = AmountBlue,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Pesos chilenos (CLP)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Monto en CLP") },
                    placeholder = { Text("Ejemplo: 15000") },
                    leadingIcon = {
                        Text(
                            text = "$",
                            color = AmountBlue,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    isError = errorMessage.isNotEmpty()
                )

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmountGreen
                    )
                ) {
                    Text(
                        text = "Continuar",
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Volver",
                        color = AmountBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Pago simulado · No se realizará ningún cargo real.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SaleAmountScreenPreview() {
    MaterialTheme {
        SaleAmountContent(
            amount = "15000",
            onAmountChange = {},
            errorMessage = "",
            onContinue = {},
            onBack = {}
        )
    }
}
