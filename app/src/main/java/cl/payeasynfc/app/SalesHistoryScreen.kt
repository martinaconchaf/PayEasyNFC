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
    onSelectSale: (SaleTransaction) -> Unit,

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
    var amountQuery by remember {
        mutableStateOf("")
    }

    var searchAmount by remember {
        mutableStateOf<Long?>(null)
    }
    var sortByAmount by remember {
        mutableStateOf("DESC")
    }

    val filteredSales = remember(
        sales,
        searchDate,
        searchAmount
    ) {
        sales.filter { sale ->

            val matchesDate = if (searchDate == null) {
                true
            } else {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = searchDate!!
                    add(Calendar.DAY_OF_MONTH, 1)
                }

                sale.createdAt >= searchDate!! &&
                        sale.createdAt < calendar.timeInMillis
            }

            val matchesAmount = searchAmount == null ||
                    sale.amount == searchAmount

            matchesDate && matchesAmount
        }
    }
    val sortedSales = remember(filteredSales, sortByAmount) {
        when (sortByAmount) {
            "ASC" -> filteredSales.sortedBy { it.amount }
            else -> filteredSales.sortedByDescending { it.amount }
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

        OutlinedTextField(
            value = amountQuery,
            onValueChange = {
                if (it.all { character -> character.isDigit() }) {
                    amountQuery = it
                    errorMessage = ""
                }
            },
            label = {
                Text("Buscar por monto")
            },
            placeholder = {
                Text("Ejemplo: 10000")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val amount = amountQuery.toLongOrNull()

                if (amount == null || amount <= 0L) {
                    searchAmount = null
                    errorMessage = "Ingresa un monto válido mayor que cero."
                } else {
                    searchAmount = amount
                    errorMessage = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Buscar por monto")
        }

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

        Text(
            text = "Ordenar ventas por monto",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    sortByAmount = "DESC"
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Mayor a menor")
            }

            Button(
                onClick = {
                    sortByAmount = "ASC"
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Menor a mayor")
            }
        }

        OutlinedButton(
            onClick = {
                dateQuery = ""
                searchDate = null
                amountQuery = ""
                searchAmount = null
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

        var amountQuery by remember {
            mutableStateOf("")
        }

        var searchAmount by remember {
            mutableStateOf<Long?>(null)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredSales.isEmpty()) {
            Text(
                text = if (
                    searchDate != null || searchAmount != null
                ) {
                    "No se encontraron ventas con los criterios ingresados."
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
                    sortedSales,
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