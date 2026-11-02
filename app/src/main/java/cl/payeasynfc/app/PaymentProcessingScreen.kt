
package cl.payeasynfc.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PaymentProcessingScreen(
    amount: Long,
    paymentMethod: String,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    var processed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Procesamiento del cobro",
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Monto: $${amount} CLP")
        Text("Método de pago: $paymentMethod")

        if (!processed) {
            Text(
                text = "Verifica los datos antes de simular el procesamiento."
            )

            Button(
                onClick = { processed = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simular procesamiento")
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        } else {
            Text(
                text = "Operación aprobada (simulación)",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "La operación se ha procesado correctamente " +
                        "en el entorno de demostración. No se realizó " +
                        "ningún cargo real."
            )

            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Finalizar")
            }
        }
    }
}
