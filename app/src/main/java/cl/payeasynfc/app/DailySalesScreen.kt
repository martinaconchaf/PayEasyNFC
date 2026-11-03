
package cl.payeasynfc.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DailySalesScreen(
    sellerEmail: String,
    database: SellerDatabase,
    onBack: () -> Unit
) {
    var selectedDate by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    var dailyTotal by remember {
        mutableLongStateOf(0L)
    }

    var dailyCount by remember {
        mutableIntStateOf(0)
    }

    var monthlyTotal by remember {
        mutableLongStateOf(0L)
    }

    var message by remember {
        mutableStateOf("")
    }

    var monthlyCount by remember {
        mutableIntStateOf(0)
    }

    var startDateText by remember {
        mutableStateOf("")
    }

    var endDateText by remember {
        mutableStateOf("")
    }

    var periodTotal by remember {
        mutableLongStateOf(0L)
    }

    var periodCount by remember {
        mutableIntStateOf(0)
    }

    var periodAverage by remember {
        mutableDoubleStateOf(0.0)
    }

    var periodMessage by remember {
        mutableStateOf("")
    }

    fun calculateDailyTotal() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDate
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

    fun calculateMonthlyTotal() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDate
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

        periodAverage = if (periodCount > 0) {
            periodTotal.toDouble() / periodCount
        } else {
            0.0
        }

        periodMessage = "Consulta del período realizada correctamente."
    }

    val formattedDate = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    ).format(Date(selectedDate))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Estadísticas de ventas",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Fecha seleccionada: $formattedDate",
            style = MaterialTheme.typography.titleMedium
        )

        Button(
            onClick = {
                selectedDate -= 24 * 60 * 60 * 1000L
                calculateDailyTotal()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Día anterior")
        }

        Button(
            onClick = {
                selectedDate += 24 * 60 * 60 * 1000L
                calculateDailyTotal()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Día siguiente")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                calculateDailyTotal()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Consultar total del día")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { calculateMonthlyTotal() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Consultar total del mes")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "TOTAL VENDIDO",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "$${dailyTotal}",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "CANTIDAD DE VENTAS",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = dailyCount.toString(),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "TOTAL VENDIDO EN EL MES",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "$${monthlyTotal}",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.secondary
        )

        if (message.isNotEmpty()) {
            Text(message)
        }

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ESTADÍSTICAS POR PERÍODO",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = startDateText,
            onValueChange = { startDateText = it },
            label = { Text("Fecha inicial (dd/MM/yyyy)") },
            placeholder = { Text("01/11/2026") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = endDateText,
            onValueChange = { endDateText = it },
            label = { Text("Fecha final (dd/MM/yyyy)") },
            placeholder = { Text("30/11/2026") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = { calculatePeriodSummary() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Consultar período")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "TOTAL DEL PERÍODO: $$periodTotal",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "CANTIDAD DE VENTAS: $periodCount",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "PROMEDIO POR VENTA: $${"%.2f".format(Locale.getDefault(), periodAverage)}",
            style = MaterialTheme.typography.titleMedium
        )

        if (periodMessage.isNotEmpty()) {
            Text(
                text = periodMessage,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}
