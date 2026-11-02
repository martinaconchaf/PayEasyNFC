
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PaymentReceiptScreen(
    transactionId: String,
    amount: Long,
    paymentMethod: String,
    onFinish: () -> Unit
) {
    val date = SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale("es", "CL")
    ).format(Date())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Comprobante de pago",
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Operación: $transactionId")
        Text("Fecha: $date")
        Text("Monto: $${amount} CLP")
        Text("Método de pago: $paymentMethod")

        Text(
            text = "Estado: APROBADA (SIMULACIÓN)",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Este comprobante corresponde a una operación " +
                    "simulada. No representa un cargo bancario real."
        )

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Finalizar")
        }
    }
}
