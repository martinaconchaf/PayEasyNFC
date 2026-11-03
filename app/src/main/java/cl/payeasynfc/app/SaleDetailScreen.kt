
package cl.payeasynfc.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DetailBlue = Color(0xFF173B65)
private val DetailBackground = Color(0xFFF3F6FA)
private val DetailGreen = Color(0xFF21865B)
private val DetailText = Color(0xFF263445)

@Composable
fun SaleDetailScreen(
    sale: SaleTransaction,
    onBack: () -> Unit
) {
    val date = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale("es", "CL")
    ).format(Date(sale.createdAt))

    val time = SimpleDateFormat(
        "HH:mm:ss",
        Locale("es", "CL")
    ).format(Date(sale.createdAt))

    val formattedAmount = NumberFormat.getCurrencyInstance(
        Locale("es", "CL")
    ).apply {
        maximumFractionDigits = 0
    }.format(sale.amount)

    SaleDetailContent(
        transactionCode = sale.transactionCode,
        amount = formattedAmount,
        paymentMethod = sale.paymentMethod,
        status = sale.status,
        date = date,
        time = time,
        onBack = onBack
    )
}

@Composable
private fun SaleDetailContent(
    transactionCode: String,
    amount: String,
    paymentMethod: String,
    status: String,
    date: String,
    time: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DetailBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Detalle de venta",
            style = MaterialTheme.typography.headlineMedium,
            color = DetailBlue,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Consulta la información de esta transacción.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

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
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MONTO DE LA VENTA",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = amount,
                        style = MaterialTheme.typography.headlineLarge,
                        color = DetailGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider(color = DetailBackground)

                DetailInfoRow(
                    label = "Identificador",
                    value = transactionCode
                )

                DetailInfoRow(
                    label = "Medio de pago",
                    value = paymentMethod
                )

                DetailInfoRow(
                    label = "Estado",
                    value = status
                )

                DetailInfoRow(
                    label = "Fecha",
                    value = date
                )

                DetailInfoRow(
                    label = "Hora",
                    value = time
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Información correspondiente a una operación simulada.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DetailBlue
            )
        ) {
            Text(
                text = "Volver al historial",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DetailInfoRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = DetailText,
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun SaleDetailScreenPreview() {
    MaterialTheme {
        SaleDetailContent(
            transactionCode = "PAY-2026-00125",
            amount = "$15.000",
            paymentMethod = "Débito",
            status = "Aprobada",
            date = "03/11/2026",
            time = "14:35:20",
            onBack = {}
        )
    }
}

