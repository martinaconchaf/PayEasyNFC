
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

        message = "Consulta mensual realizada correctamente."
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

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}
