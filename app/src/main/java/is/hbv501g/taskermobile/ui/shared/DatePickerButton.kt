package `is`.hbv501g.taskermobile.ui.shared

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun DatePickerButton(
    selectedDate: LocalDateTime?,
    onDateSelected: (LocalDateTime) -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    // Use the current date or the selected date to initialize the DatePicker
    val calendar = Calendar.getInstance().apply {
        if (selectedDate != null) {
            time = Date.from(selectedDate.atZone(ZoneId.systemDefault()).toInstant())
        }
    }

    if (showDialog) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                // month is zero-based, so add 1
                val newDate = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0)
                onDateSelected(newDate)
                showDialog = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Button(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        val displayText = selectedDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: "Select Date"
        Text(text = displayText)
    }
}
