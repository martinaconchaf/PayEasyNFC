
package cl.payeasynfc.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.Normalizer
import java.text.NumberFormat
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val HistoryBlue = Color(0xFF173B65)
private val HistoryBackground = Color(0xFFF3F6FA)
private val HistoryGreen = Color(0xFF21865B)
private val HistoryText = Color(0xFF263445)

private fun parseSearchDate(value: String): Long? {
    val formatter = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale("es", "CL")
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

private fun normalizePaymentMethod(value: String): String {
    return Normalizer.normalize(
        value,
        Normalizer.Form.NFD
    )
        .replace("\\p{Mn}+".toRegex(), "")
        .lowercase(Locale.ROOT)
}

private fun formatHistoryAmount(amount: Long): String {
    return NumberFormat.getCurrencyInstance(
        Locale("es", "CL")
    ).apply {
        maximumFractionDigits = 0
    }.format(amount)
}

@Composable
fun SalesHistoryScreen(
    sales: List<SaleTransaction>,
    onBack: () -> Unit,
    onSelectSale: (SaleTransaction) -> Unit
) {
    var dateQuery by remember { mutableStateOf("") }
    var searchDate by remember { mutableStateOf<Long?>(null) }
    var amountQuery by remember { mutableStateOf("") }
    var searchAmount by remember { mutableStateOf<Long?>(null) }
    var errorMessage by remember { mutableStateOf("") }

    var sortMode by remember { mutableStateOf("AMOUNT_DESC") }
    var cardFilter by remember { mutableStateOf("ALL") }

    val filteredSales = remember(
        sales,
        searchDate,
        searchAmount,
        cardFilter
    ) {
        sales.filter { sale ->

            val matchesDate = if (searchDate == null) {
                true
            } else {
                val endOfDay = Calendar.getInstance().apply {
                    timeInMillis = searchDate!!
                    add(Calendar.DAY_OF_MONTH, 1)
                }.timeInMillis

                sale.createdAt >= searchDate!! &&
                        sale.createdAt < endOfDay
            }

            val matchesAmount =
                searchAmount == null || sale.amount == searchAmount

            val normalizedMethod = normalizePaymentMethod(
                sale.paymentMethod
            )

            val matchesCard = when (cardFilter) {
                "DEBIT" -> normalizedMethod.contains("deb")
                "CREDIT" -> normalizedMethod.contains("cred")
                else -> true
            }

            matchesDate && matchesAmount && matchesCard
        }
    }

    val sortedSales = remember(filteredSales, sortMode) {
        when (sortMode) {
            "AMOUNT_ASC" ->
                filteredSales.sortedBy { it.amount }

            "DATE_DESC" ->
                filteredSales.sortedByDescending { it.createdAt }

            "DATE_ASC" ->
                filteredSales.sortedBy { it.createdAt }

            else ->
                filteredSales.sortedByDescending { it.amount }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HistoryBackground),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 20.dp,
            vertical = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Historial de ventas",
                    style = MaterialTheme.typography.headlineMedium,
                    color = HistoryBlue,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Consulta, busca y organiza tus operaciones.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Buscar ventas",
                        style = MaterialTheme.typography.titleLarge,
                        color = HistoryBlue,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = dateQuery,
                        onValueChange = {
                            dateQuery = it
                            errorMessage = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Buscar por fecha") },
                        placeholder = { Text("dd/MM/yyyy") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            val parsedDate = parseSearchDate(
                                dateQuery.trim()
                            )

                            if (parsedDate == null) {
                                searchDate = null
                                errorMessage =
                                    "Ingresa una fecha válida con formato dd/MM/yyyy."
                            } else {
                                searchDate = parsedDate
                                errorMessage = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HistoryBlue
                        )
                    ) {
                        Text("Buscar por fecha")
                    }

                    HorizontalDivider(color = HistoryBackground)

                    OutlinedTextField(
                        value = amountQuery,
                        onValueChange = { value ->
                            if (value.all { it.isDigit() }) {
                                amountQuery = value
                                errorMessage = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Buscar por monto (CLP)") },
                        placeholder = { Text("Ejemplo: 10000") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            val amount = amountQuery.toLongOrNull()

                            if (amount == null || amount <= 0L) {
                                searchAmount = null
                                errorMessage =
                                    "Ingresa un monto válido mayor que cero."
                            } else {
                                searchAmount = amount
                                errorMessage = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HistoryBlue
                        )
                    ) {
                        Text("Buscar por monto")
                    }

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            dateQuery = ""
                            searchDate = null
                            amountQuery = ""
                            searchAmount = null
                            cardFilter = "ALL"
                            sortMode = "AMOUNT_DESC"
                            errorMessage = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Limpiar búsqueda y filtros")
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Ordenar ventas",
                        style = MaterialTheme.typography.titleLarge,
                        color = HistoryBlue,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Por monto",
                        style = MaterialTheme.typography.titleSmall,
                        color = HistoryText
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = sortMode == "AMOUNT_DESC",
                            onClick = { sortMode = "AMOUNT_DESC" },
                            label = { Text("Mayor a menor") }
                        )

                        FilterChip(
                            selected = sortMode == "AMOUNT_ASC",
                            onClick = { sortMode = "AMOUNT_ASC" },
                            label = { Text("Menor a mayor") }
                        )
                    }

                    Text(
                        text = "Por fecha",
                        style = MaterialTheme.typography.titleSmall,
                        color = HistoryText
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = sortMode == "DATE_DESC",
                            onClick = { sortMode = "DATE_DESC" },
                            label = { Text("Recientes") }
                        )

                        FilterChip(
                            selected = sortMode == "DATE_ASC",
                            onClick = { sortMode = "DATE_ASC" },
                            label = { Text("Antiguas") }
                        )
                    }

                    HorizontalDivider(color = HistoryBackground)

                    Text(
                        text = "Medio de pago",
                        style = MaterialTheme.typography.titleSmall,
                        color = HistoryText
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = cardFilter == "ALL",
                            onClick = { cardFilter = "ALL" },
                            label = { Text("Todas") }
                        )

                        FilterChip(
                            selected = cardFilter == "DEBIT",
                            onClick = { cardFilter = "DEBIT" },
                            label = { Text("Débito") }
                        )

                        FilterChip(
                            selected = cardFilter == "CREDIT",
                            onClick = { cardFilter = "CREDIT" },
                            label = { Text("Crédito") }
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = HistoryBlue
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Operaciones encontradas",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = filteredSales.size.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (sortedSales.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sin resultados",
                            style = MaterialTheme.typography.titleLarge,
                            color = HistoryBlue,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (
                                searchDate != null ||
                                searchAmount != null ||
                                cardFilter != "ALL"
                            ) {
                                "No se encontraron ventas con los criterios ingresados."
                            } else {
                                "Todavía no tienes ventas registradas."
                            },
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "Tus operaciones",
                    style = MaterialTheme.typography.titleLarge,
                    color = HistoryBlue,
                    fontWeight = FontWeight.Bold
                )
            }

            items(
                items = sortedSales,
                key = { it.id }
            ) { sale ->
                SaleHistoryCard(
                    sale = sale,
                    onClick = { onSelectSale(sale) }
                )
            }
        }

        item {
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HistoryBlue
                )
            ) {
                Text(
                    text = "Volver al perfil",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SaleHistoryCard(
    sale: SaleTransaction,
    onClick: () -> Unit
) {
    val date = SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale("es", "CL")
    ).format(Date(sale.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Venta",
                    style = MaterialTheme.typography.titleMedium,
                    color = HistoryBlue,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = sale.status,
                    style = MaterialTheme.typography.labelMedium,
                    color = HistoryGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = formatHistoryAmount(sale.amount),
                style = MaterialTheme.typography.headlineSmall,
                color = HistoryGreen,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider(color = HistoryBackground)

            Text(
                text = "Código: ${sale.transactionCode}",
                style = MaterialTheme.typography.bodyMedium,
                color = HistoryText
            )

            Text(
                text = "Medio de pago: ${sale.paymentMethod}",
                style = MaterialTheme.typography.bodyMedium,
                color = HistoryText
            )

            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Text(
                text = "Toca para ver el detalle",
                style = MaterialTheme.typography.labelSmall,
                color = HistoryBlue,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SalesHistoryScreenPreview() {
    val sampleSales = listOf(
        SaleTransaction(
            id = 1L,
            transactionCode = "PAY-2026-00125",
            amount = 15000L,
            paymentMethod = "Débito",
            status = "Aprobada",
            createdAt = System.currentTimeMillis()
        ),
        SaleTransaction(
            id = 2L,
            transactionCode = "PAY-2026-00126",
            amount = 25000L,
            paymentMethod = "Crédito",
            status = "Aprobada",
            createdAt = System.currentTimeMillis() - 3600000L
        )
    )

    MaterialTheme {
        SalesHistoryScreen(
            sales = sampleSales,
            onBack = {},
            onSelectSale = {}
        )
    }
}
