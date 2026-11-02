package cl.payeasynfc.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SaleDetailScreen(
    sale: SaleTransaction,
    onBack: () -> Unit
) {
    val date = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    ).format(Date(sale.createdAt))

    val time = SimpleDateFormat(
        "HH:mm:ss",
        Locale.getDefault()
    ).format(Date(sale.createdAt))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Detalle de venta",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Identificador de transacción")
        Text(
            text = sale.transactionCode,
            style = MaterialTheme.typography.titleMedium
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Text("Monto de la venta")
        Text(
            text = "$${sale.amount}",
            style = MaterialTheme.typography.headlineSmall
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Text("Medio de pago")
        Text(sale.paymentMethod)

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Text("Estado de la operación")
        Text(sale.status)

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Text("Fecha")
        Text(date)

        Spacer(modifier = Modifier.height(8.dp))

        Text("Hora")
        Text(time)

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al historial")
        }
    }
}