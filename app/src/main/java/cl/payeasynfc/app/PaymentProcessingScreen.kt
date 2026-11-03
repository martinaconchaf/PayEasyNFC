
package cl.payeasynfc.app

import java.text.NumberFormat
import java.util.Locale

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val ProcessingBlue = Color(0xFF173B65)
private val ProcessingBackground = Color(0xFFF3F6FA)
private val ProcessingGreen = Color(0xFF23865B)
private val ProcessingRed = Color(0xFFC43D4B)

@Composable
fun PaymentProcessingScreen(
    amount: Long,
    paymentMethod: String,
    onResult: (Boolean, String) -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit
) {
    PaymentProcessingContent(
        amount = amount,
        paymentMethod = paymentMethod,
        onApprove = {
            onResult(
                true,
                "El pago fue aprobado correctamente."
            )
        },
        onReject = {
            onResult(
                false,
                "La operación fue rechazada por el proveedor simulado."
            )
        },
        onCancel = onCancel,
        onBack = onBack
    )
}

@Composable
private fun PaymentProcessingContent(
    amount: Long,
    paymentMethod: String,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit
) {
    val formattedAmount = NumberFormat
        .getIntegerInstance(Locale("es", "CL"))
        .format(amount)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProcessingBackground)
            .verticalScroll(
                androidx.compose.foundation.rememberScrollState()
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "PayEasy NFC",
                color = ProcessingBlue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Procesamiento del cobro",
                color = ProcessingBlue,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Revisa los datos de la operación antes de continuar.",
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "MONTO A COBRAR",
                    color = Color(0xFF64748B),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "$$formattedAmount",
                    color = ProcessingBlue,
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

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Método de pago",
                        color = Color(0xFF64748B),
                        style = MaterialTheme.typography.labelMedium
                    )

                    Text(
                        text = paymentMethod,
                        color = ProcessingBlue,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFEAF1F8)
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Simulación de pago",
                    color = ProcessingBlue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Selecciona uno de los resultados para continuar con la demostración de la operación.",
                    color = Color(0xFF475569),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Esta operación es simulada y no realiza un cargo bancario real.",
                    color = Color(0xFF64748B),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Resultado de la operación",
                color = ProcessingBlue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = onApprove,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProcessingGreen
                )
            ) {
                Text(
                    text = "Simular pago aprobado",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = onReject,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProcessingRed
                )
            ) {
                Text(
                    text = "Simular pago rechazado",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Cancelar cobro")
        }

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Volver a la pantalla anterior",
                color = ProcessingBlue
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PaymentProcessingScreenPreview() {
    MaterialTheme {
        PaymentProcessingContent(
            amount = 12990L,
            paymentMethod = "Débito",
            onApprove = {},
            onReject = {},
            onCancel = {},
            onBack = {}
        )
    }
}
