
package cl.payeasynfc.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SaleAmountScreen(
    onAmountConfirmed: (Long) -> Unit,
    onBack: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Nuevo cobro",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Ingresa el monto de la venta en pesos chilenos."
        )

        OutlinedTextField(
            value = amount,
            onValueChange = {
                amount = it
                errorMessage = ""
            },
            label = { Text("Monto de la venta (CLP)") },
            placeholder = { Text("Ejemplo: 15000") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {
                val cleanAmount = amount.trim()

                if (cleanAmount.isEmpty()) {
                    errorMessage = "Debes ingresar un monto."
                } else if (!cleanAmount.all { it.isDigit() }) {
                    errorMessage = "Ingresa solamente números enteros positivos."
                } else {
                    val numericAmount = cleanAmount.toLongOrNull()

                    if (numericAmount == null) {
                        errorMessage = "El monto ingresado no es válido."
                    } else if (numericAmount <= 0L) {
                        errorMessage = "El monto debe ser mayor que cero."
                    } else {
                        onAmountConfirmed(numericAmount)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar")
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}
