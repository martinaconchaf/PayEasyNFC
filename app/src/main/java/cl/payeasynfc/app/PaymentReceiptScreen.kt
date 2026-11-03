
package cl.payeasynfc.app

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val ReceiptBlue = Color(0xFF173B65)
private val ReceiptBackground = Color(0xFFF3F6FA)
private val ReceiptGreen = Color(0xFF23865B)

@Composable
fun PaymentReceiptScreen(
    transactionId: String,
    amount: Long,
    paymentMethod: String,
    createdAt: Long,
    onSaveReceipt: () -> Unit,
    onShareReceipt: () -> Unit,
    onShareSellerCopy: () -> Unit,
    onFinish: () -> Unit
) {
    val date = SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale("es", "CL")
    ).format(Date(createdAt))

    val formattedAmount = NumberFormat
        .getIntegerInstance(Locale("es", "CL"))
        .format(amount)

    PaymentReceiptContent(
        transactionId = transactionId,
        date = date,
        formattedAmount = formattedAmount,
        paymentMethod = paymentMethod,
        onSaveReceipt = onSaveReceipt,
        onShareReceipt = onShareReceipt,
        onShareSellerCopy = onShareSellerCopy,
        onFinish = onFinish
    )
}

@Composable
private fun PaymentReceiptContent(
    transactionId: String,
    date: String,
    formattedAmount: String,
    paymentMethod: String,
    onSaveReceipt: () -> Unit,
    onShareReceipt: () -> Unit,
    onShareSellerCopy: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReceiptBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "PayEasy NFC",
                color = ReceiptBlue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Comprobante de pago",
                color = ReceiptBlue,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Resumen de la operación realizada.",
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "OPERACIÓN APROBADA",
                    color = ReceiptGreen,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Pago aprobado",
                    color = ReceiptBlue,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "$$formattedAmount",
                    color = ReceiptBlue,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Pesos chilenos (CLP)",
                    color = Color(0xFF64748B),
                    style = MaterialTheme.typography.bodyMedium
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color(0xFFE7ECF2)
                )

                ReceiptDataRow(
                    label = "Número de operación",
                    value = transactionId
                )

                ReceiptDataRow(
                    label = "Fecha y hora",
                    value = date
                )

                ReceiptDataRow(
                    label = "Método de pago",
                    value = paymentMethod
                )

                ReceiptDataRow(
                    label = "Estado",
                    value = "Aprobada (simulación)",
                    valueColor = ReceiptGreen
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFEAF1F8)
            )
        ) {
            Text(
                text = "Este comprobante corresponde a una operación simulada. No representa un cargo bancario real.",
                modifier = Modifier.padding(16.dp),
                color = Color(0xFF475569),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Start
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Acciones del comprobante",
                color = ReceiptBlue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = onSaveReceipt,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ReceiptBlue
                )
            ) {
                Text("Guardar comprobante PDF")
            }

            OutlinedButton(
                onClick = onShareReceipt,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Enviar comprobante")
            }

            OutlinedButton(
                onClick = onShareSellerCopy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Enviar copia al vendedor")
            }
        }

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ReceiptGreen
            )
        ) {
            Text(
                text = "Volver al menú principal",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ReceiptDataRow(
    label: String,
    value: String,
    valueColor: Color = ReceiptBlue
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            color = Color(0xFF64748B),
            style = MaterialTheme.typography.labelMedium
        )

        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PaymentReceiptScreenPreview() {
    MaterialTheme {
        PaymentReceiptContent(
            transactionId = "PAY-2026-00125",
            date = "03/11/2026 16:30",
            formattedAmount = "12.990",
            paymentMethod = "Débito",
            onSaveReceipt = {},
            onShareReceipt = {},
            onShareSellerCopy = {},
            onFinish = {}
        )
    }
}
