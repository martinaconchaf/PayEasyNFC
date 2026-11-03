
package cl.payeasynfc.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ResultBlue = Color(0xFF173B65)
private val ResultBackground = Color(0xFFF3F6FA)
private val ResultGreen = Color(0xFF21865B)
private val ResultRed = Color(0xFFC62828)

@Composable
fun PaymentResultScreen(
    approved: Boolean,
    message: String,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    PaymentResultContent(
        approved = approved,
        message = message,
        onContinue = onContinue,
        onBack = onBack
    )
}

@Composable
private fun PaymentResultContent(
    approved: Boolean,
    message: String,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val resultColor = if (approved) ResultGreen else ResultRed
    val resultTitle = if (approved) "¡Pago aprobado!" else "Pago rechazado"
    val resultIcon = if (approved) "✓" else "✕"
    val continueText = if (approved) "Ver comprobante" else "Intentar nuevamente"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ResultBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "PayEasy NFC",
            style = MaterialTheme.typography.titleLarge,
            color = ResultBlue,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

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
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .background(resultColor, CircleShape)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = resultIcon,
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = resultTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = resultColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF526174),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (approved) {
                        "La operación puede continuar al comprobante."
                    } else {
                        "No se generará un comprobante de pago aprobado."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF718096),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = ResultBlue
                    )
                ) {
                    Text(
                        text = continueText,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Volver al perfil")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "PayEasy NFC · Simulación de pagos",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF8995A5),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PaymentResultApprovedPreview() {
    PaymentResultContent(
        approved = true,
        message = "La operación fue aprobada correctamente.",
        onContinue = {},
        onBack = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun PaymentResultRejectedPreview() {
    PaymentResultContent(
        approved = false,
        message = "La operación no pudo ser aprobada. Puedes intentarlo nuevamente.",
        onContinue = {},
        onBack = {}
    )
}
