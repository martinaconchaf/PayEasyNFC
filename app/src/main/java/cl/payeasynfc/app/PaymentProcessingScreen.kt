
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PaymentProcessingScreen(
    amount: Long,
    paymentMethod: String,
    onResult: (Boolean, String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "Procesamiento del cobro",
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Monto: $${amount} CLP")
        Text("Método de pago: $paymentMethod")

        Text(
            text = "Esperando respuesta del proveedor (simulación)."
        )

        Button(
            onClick = {
                onResult(
                    true,
                    "El pago fue aprobado correctamente."
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simular pago aprobado")
        }

        Button(
            onClick = {
                onResult(
                    false,
                    "La operación fue rechazada por el proveedor simulado."
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simular pago rechazado")
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar y volver")
        }
    }
}
