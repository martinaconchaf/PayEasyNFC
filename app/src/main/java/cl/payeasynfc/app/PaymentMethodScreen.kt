
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
fun PaymentMethodScreen(
    amount: Long,
    onMethodSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Método de pago",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Monto de la venta: $${amount} CLP",
            style = MaterialTheme.typography.titleLarge
        )

        Text("Selecciona cómo pagará el cliente:")

        Button(
            onClick = { onMethodSelected("Débito") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tarjeta de débito")
        }

        Button(
            onClick = { onMethodSelected("Crédito") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tarjeta de crédito")
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}
