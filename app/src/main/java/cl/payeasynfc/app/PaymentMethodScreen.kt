
package cl.payeasynfc.app

import java.text.NumberFormat
import java.util.Locale

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val PaymentMethodBlue = Color(0xFF173B65)
private val PaymentMethodBackground = Color(0xFFF3F6FA)
private val PaymentMethodGreen = Color(0xFF23865B)

@Composable
fun PaymentMethodScreen(
    amount: Long,
    onMethodSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    PaymentMethodContent(
        amount = amount,
        onMethodSelected = onMethodSelected,
        onBack = onBack
    )
}

@Composable
private fun PaymentMethodContent(
    amount: Long,
    onMethodSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val formattedAmount = NumberFormat
        .getIntegerInstance(Locale("es", "CL"))
        .format(amount)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaymentMethodBackground)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "PayEasy NFC",
                color = PaymentMethodBlue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Método de pago",
                color = PaymentMethodBlue,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Selecciona cómo pagará tu cliente.",
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = PaymentMethodBlue
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "MONTO DE LA VENTA",
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "$$formattedAmount",
                    color = Color.White,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Pesos chilenos (CLP)",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Text(
            text = "Elige una opción",
            color = PaymentMethodBlue,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        PaymentOptionCard(
            title = "Tarjeta de débito",
            description = "El cliente pagará utilizando su tarjeta de débito.",
            label = "DÉBITO",
            onClick = {
                onMethodSelected("Débito")
            }
        )

        PaymentOptionCard(
            title = "Tarjeta de crédito",
            description = "El cliente pagará utilizando su tarjeta de crédito.",
            label = "CRÉDITO",
            onClick = {
                onMethodSelected("Crédito")
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Volver")
        }
    }
}

@Composable
private fun PaymentOptionCard(
    title: String,
    description: String,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = Color(0xFFEAF1F8),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (label == "DÉBITO") "D" else "C",
                    color = PaymentMethodBlue,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = title,
                    color = PaymentMethodBlue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = description,
                    color = Color(0xFF64748B),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "›",
                color = PaymentMethodGreen,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PaymentMethodScreenPreview() {
    MaterialTheme {
        PaymentMethodContent(
            amount = 12990L,
            onMethodSelected = {},
            onBack = {}
        )
    }
}
