package cl.payeasynfc.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun parseSearchDate(value: String): Long? {
    val formatter = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    ).apply {
        isLenient = false
    }

    val position = ParsePosition(0)
    val date = formatter.parse(value, position)

    if (date == null || position.index != value.length) {
        return null
    }

    val calendar = Calendar.getInstance().apply {
        time = date
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    return calendar.timeInMillis
}

@Composable
fun SalesHistoryScreen(
    sales: List<SaleTransaction>,
    onBack: () -> Unit,
    onSelectSale: (SaleTransaction) -> Unit
) {
    var dateQuery by remember {
        mutableStateOf("")
    }

    var searchDate by remember {
        mutableStateOf<Long?>(null)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    val filteredSales = remember(sales, searchDate) {
        if (searchDate == null) {
            sales
        } else {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = searchDate!!
                add(Calendar.DAY_OF_MONTH, 1)
            }

            val nextDay = calendar.timeInMillis

            sales.filter { sale ->
                sale.createdAt >= searchDate!! &&
                        sale.createdAt < nextDay
            }
        }
    }

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

        OutlinedTextField(
            value = dateQuery,
            onValueChange = {
                dateQuery = it
                errorMessage = ""
            },
            label = {
                Text("Buscar por fecha")
            },
            placeholder = {
                Text("dd/MM/yyyy")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val parsedDate = parseSearchDate(dateQuery.trim())

                if (parsedDate == null) {
                    searchDate = null
                    errorMessage =
                        "Ingresa una fecha válida con formato dd/MM/yyyy."
                } else {
                    searchDate = parsedDate
                    errorMessage = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Buscar")
        }

        OutlinedButton(
            onClick = {
                dateQuery = ""
                searchDate = null
                errorMessage = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Limpiar búsqueda")
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredSales.isEmpty()) {
            Text(
                text = if (searchDate != null) {
                    "No se encontraron ventas para esa fecha."
                } else {
                    "Todavía no tienes ventas registradas."
                }
            )
        } else {
            Text("Operaciones encontradas: ${filteredSales.size}")

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    filteredSales,
                    key = { it.id }
                ) { sale ->

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