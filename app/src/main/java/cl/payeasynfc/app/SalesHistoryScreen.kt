package cl.payeasynfc.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesHistoryScreen(
    sales: List<SaleTransaction>,
    onBack: () -> Unit,
    onSelectSale: (SaleTransaction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Historial de ventas",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (sales.isEmpty()) {
            Text("Todavía no tienes ventas registradas.")
        } else {
            Text("Total de operaciones: ${sales.size}")

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sales, key = { it.id }) { sale ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectSale(sale)
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Monto: $${sale.amount}",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text("Código: ${sale.transactionCode}")
                            Text("Tarjeta: ${sale.paymentMethod}")
                            Text("Estado: ${sale.status}")

                            val date = SimpleDateFormat(
                                "dd/MM/yyyy HH:mm",
                                Locale.getDefault()
                            ).format(Date(sale.createdAt))

                            Text("Fecha: $date")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al perfil")
        }
    }
}