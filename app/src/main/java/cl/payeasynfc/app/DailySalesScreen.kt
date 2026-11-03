
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val SalesBlue = Color(0xFF173B65)
private val SalesBackground = Color(0xFFF3F6FA)
private val SalesGreen = Color(0xFF21865B)
private val SalesPurple = Color(0xFF6954A3)
private val SalesText = Color(0xFF526174)

private fun formatSalesCurrency(amount: Number): String {
    return NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }.format(amount)
}

@Composable
fun DailySalesScreen(
    sellerEmail: String,
    database: SellerDatabase,
    onBack: () -> Unit
) {
    var selectedDate by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    var dailyTotal by remember { mutableLongStateOf(0L) }
    var dailyCount by remember { mutableIntStateOf(0) }
    var monthlyTotal by remember { mutableLongStateOf(0L) }
    var monthlyCount by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("") }

    var startDateText by remember { mutableStateOf("") }
    var endDateText by remember { mutableStateOf("") }

    var periodTotal by remember { mutableLongStateOf(0L) }
    var periodCount by remember { mutableIntStateOf(0) }
    var periodAverage by remember { mutableDoubleStateOf(0.0) }
    var periodMessage by remember { mutableStateOf("") }

    var maximumSale by remember { mutableStateOf<Long?>(null) }
    var minimumSale by remember { mutableStateOf<Long?>(null) }

    var debitCount by remember { mutableIntStateOf(0) }
    var creditCount by remember { mutableIntStateOf(0) }
    var debitAmount by remember { mutableLongStateOf(0L) }
    var creditAmount by remember { mutableLongStateOf(0L) }

    fun calculateDailyTotal(dateToUse: Long = selectedDate) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateToUse
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val startOfNextDay = calendar.timeInMillis

        dailyTotal = database.getDailySalesTotal(
            sellerEmail,
            startOfDay,
            startOfNextDay
        )

        dailyCount = database.getDailySalesCount(
            sellerEmail,
            startOfDay,
            startOfNextDay
        )

        message = "Consulta realizada correctamente."
    }

    fun calculateMonthlyTotal(dateToUse: Long = selectedDate) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateToUse
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startOfMonth = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        val startOfNextMonth = calendar.timeInMillis

        monthlyTotal = database.getMonthlySalesTotal(
            sellerEmail,
            startOfMonth,
            startOfNextMonth
        )

        monthlyCount = database.getMonthlySalesCount(
            sellerEmail,
            startOfMonth,
            startOfNextMonth
        )

        message = "Consulta mensual realizada correctamente."
    }

    fun calculatePeriodSummary() {
        val dateFormat = SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        ).apply {
            isLenient = false
        }

        val startDate = try {
            dateFormat.parse(startDateText)
        } catch (e: Exception) {
            null
        }

        val endDate = try {
            dateFormat.parse(endDateText)
        } catch (e: Exception) {
            null
        }

        if (startDate == null || endDate == null) {
            periodMessage = "Ingresa fechas válidas en formato dd/MM/yyyy."
            return
        }

        val startCalendar = Calendar.getInstance().apply {
            time = startDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endCalendar = Calendar.getInstance().apply {
            time = endDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (endCalendar.before(startCalendar)) {
            periodMessage = "La fecha final no puede ser anterior a la inicial."
            return
        }

        val startTimestamp = startCalendar.timeInMillis
        endCalendar.add(Calendar.DAY_OF_MONTH, 1)
        val endTimestampExclusive = endCalendar.timeInMillis

        val result = database.getSalesPeriodSummary(
            sellerEmail,
            startTimestamp,
            endTimestampExclusive
        )

        periodTotal = result.first
        periodCount = result.second

        val maxMinResult = database.getMaxMinSales(
            sellerEmail,
            startTimestamp,
            endTimestampExclusive
        )

        maximumSale = maxMinResult.first
        minimumSale = maxMinResult.second

        val paymentCounts = database.getPaymentMethodCounts(
            sellerEmail,
            startTimestamp,
            endTimestampExclusive
        )

        debitCount = paymentCounts.first
        creditCount = paymentCounts.second

        val paymentAmounts = database.getPaymentMethodAmounts(
            sellerEmail,
            startTimestamp,
            endTimestampExclusive
        )

        debitAmount = paymentAmounts.first
        creditAmount = paymentAmounts.second

        periodAverage = if (periodCount > 0) {
            periodTotal.toDouble() / periodCount
        } else {
            0.0
        }

        periodMessage = "Consulta del período realizada correctamente."
    }

    val formattedDate = SimpleDateFormat(
        "EEEE dd 'de' MMMM 'de' yyyy",
        Locale("es", "CL")
    ).format(Date(selectedDate)).replaceFirstChar {
        it.uppercase()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SalesBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = "PayEasy NFC",
                style = MaterialTheme.typography.titleLarge,
                color = SalesBlue,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Estadísticas de ventas",
                style = MaterialTheme.typography.headlineMedium,
                color = SalesBlue,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Revisa el rendimiento de tu negocio.",
                style = MaterialTheme.typography.bodyMedium,
                color = SalesText
            )
        }

        // RESUMEN DIARIO Y MENSUAL

        SalesSectionCard(title = "Resumen de ventas") {
            Text(
                text = "Fecha seleccionada",
                style = MaterialTheme.typography.labelLarge,
                color = SalesText
            )

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleMedium,
                color = SalesBlue,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = selectedDate
                            add(Calendar.DAY_OF_MONTH, -1)
                        }
                        selectedDate = calendar.timeInMillis
                        calculateDailyTotal(selectedDate)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Día anterior")
                }

                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = selectedDate
                            add(Calendar.DAY_OF_MONTH, 1)
                        }
                        selectedDate = calendar.timeInMillis
                        calculateDailyTotal(selectedDate)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Día siguiente")
                }
            }

            Button(
                onClick = { calculateDailyTotal() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SalesBlue
                )
            ) {
                Text("Consultar total del día")
            }

            Button(
                onClick = { calculateMonthlyTotal() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SalesPurple
                )
            ) {
                Text("Consultar total del mes")
            }
        }

        // INDICADORES PRINCIPALES

        Text(
            text = "Indicadores principales",
            style = MaterialTheme.typography.titleLarge,
            color = SalesBlue,
            fontWeight = FontWeight.Bold
        )

        SalesMetricCard(
            title = "Total vendido en el día",
            value = formatSalesCurrency(dailyTotal),
            description = "Ventas de la fecha seleccionada",
            accentColor = SalesGreen
        )

        SalesMetricCard(
            title = "Cantidad de ventas del día",
            value = dailyCount.toString(),
            description = "Operaciones registradas",
            accentColor = SalesBlue
        )

        SalesMetricCard(
            title = "Total vendido en el mes",
            value = formatSalesCurrency(monthlyTotal),
            description = "Ventas del mes de la fecha seleccionada",
            accentColor = SalesPurple
        )

        SalesMetricCard(
            title = "Cantidad de ventas del mes",
            value = monthlyCount.toString(),
            description = "Operaciones registradas durante el mes",
            accentColor = SalesBlue
        )

        if (message.isNotEmpty()) {
            Text(
                text = message,
                color = SalesGreen,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // CONSULTA POR PERÍODO

        SalesSectionCard(title = "Estadísticas por período") {
            Text(
                text = "Selecciona un rango de fechas para consultar el rendimiento de tu negocio.",
                style = MaterialTheme.typography.bodyMedium,
                color = SalesText
            )

            OutlinedTextField(
                value = startDateText,
                onValueChange = { startDateText = it },
                label = { Text("Fecha inicial") },
                placeholder = { Text("01/11/2026") },
                supportingText = { Text("Formato: dd/MM/yyyy") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = endDateText,
                onValueChange = { endDateText = it },
                label = { Text("Fecha final") },
                placeholder = { Text("30/11/2026") },
                supportingText = { Text("Formato: dd/MM/yyyy") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = { calculatePeriodSummary() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SalesBlue
                )
            ) {
                Text("Consultar período")
            }

            if (periodMessage.isNotEmpty()) {
                Text(
                    text = periodMessage,
                    color = if (
                        periodMessage.startsWith("Ingresa") ||
                        periodMessage.startsWith("La fecha")
                    ) {
                        MaterialTheme.colorScheme.error
                    } else {
                        SalesGreen
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // RESULTADOS DEL PERÍODO

        Text(
            text = "Resultados del período",
            style = MaterialTheme.typography.titleLarge,
            color = SalesBlue,
            fontWeight = FontWeight.Bold
        )

        SalesMetricCard(
            title = "Total del período",
            value = formatSalesCurrency(periodTotal),
            description = "Monto total vendido",
            accentColor = SalesGreen
        )

        SalesMetricCard(
            title = "Cantidad de ventas",
            value = periodCount.toString(),
            description = "Operaciones del período",
            accentColor = SalesBlue
        )

        SalesMetricCard(
            title = "Promedio por venta",
            value = formatSalesCurrency(periodAverage),
            description = "Monto promedio de cada operación",
            accentColor = SalesPurple
        )

        SalesSectionCard(title = "Ventas destacadas") {
            SalesInfoRow(
                label = "Venta de mayor monto",
                value = maximumSale?.let {
                    formatSalesCurrency(it)
                } ?: "Sin ventas"
            )

            HorizontalDivider(color = Color(0xFFE5EAF0))

            SalesInfoRow(
                label = "Venta de menor monto",
                value = minimumSale?.let {
                    formatSalesCurrency(it)
                } ?: "Sin ventas"
            )
        }

        // MÉTODOS DE PAGO

        SalesSectionCard(title = "Cantidad de pagos por método") {
            SalesInfoRow(
                label = "Pagos con débito",
                value = debitCount.toString()
            )

            HorizontalDivider(color = Color(0xFFE5EAF0))

            SalesInfoRow(
                label = "Pagos con crédito",
                value = creditCount.toString()
            )

            HorizontalDivider(color = Color(0xFFE5EAF0))

            SalesInfoRow(
                label = "Total de pagos",
                value = (debitCount + creditCount).toString(),
                highlight = true
            )
        }

        SalesSectionCard(title = "Monto total por método de pago") {
            SalesInfoRow(
                label = "Monto con débito",
                value = formatSalesCurrency(debitAmount)
            )

            HorizontalDivider(color = Color(0xFFE5EAF0))

            SalesInfoRow(
                label = "Monto con crédito",
                value = formatSalesCurrency(creditAmount)
            )

            HorizontalDivider(color = Color(0xFFE5EAF0))

            SalesInfoRow(
                label = "Total débito y crédito",
                value = formatSalesCurrency(debitAmount + creditAmount),
                highlight = true
            )
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Volver al perfil")
        }

        Text(
            text = "PayEasy NFC · Estadísticas de ventas",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF8995A5),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SalesSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = SalesBlue,
                fontWeight = FontWeight.Bold
            )

            content()
        }
    }
}

@Composable
private fun SalesMetricCard(
    title: String,
    value: String,
    description: String,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = SalesText,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8995A5)
            )
        }
    }
}

@Composable
private fun SalesInfoRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlight) SalesBlue else SalesText,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlight) SalesBlue else SalesText,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SalesMetricCardPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SalesBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SalesMetricCard(
            title = "Total vendido en el día",
            value = "$125.000",
            description = "Ventas de la fecha seleccionada",
            accentColor = SalesGreen
        )

        SalesMetricCard(
            title = "Cantidad de ventas",
            value = "18",
            description = "Operaciones registradas",
            accentColor = SalesBlue
        )
    }
}
