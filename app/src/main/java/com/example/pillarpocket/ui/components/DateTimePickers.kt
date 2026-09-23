package com.example.pillarpocket.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pillarpocket.ui.theme.PillarGreen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PillarDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    onDateSelected(sdf.format(Date(millis)))
                }
                onDismiss()
            }) { Text("OK", color = PillarGreen) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = PillarGreen,
                todayDateBorderColor = PillarGreen
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PillarTimePickerDialog(
    title: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(is24Hour = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectorColor = PillarGreen,
                        timeSelectorSelectedContainerColor = PillarGreen
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val hour = timePickerState.hour.toString().padStart(2, '0')
                val minute = timePickerState.minute.toString().padStart(2, '0')
                onTimeSelected("$hour:$minute")
                onDismiss()
            }) { Text("OK", color = PillarGreen) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

fun formatDisplayDate(dateStr: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val display = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        display.format(sdf.parse(dateStr)!!)
    } catch (e: Exception) { dateStr }
}