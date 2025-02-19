package `is`.hbv501g.taskermobile.ui.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun PriorityDropdown(
    selectedPriority: String,
    onPrioritySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val priorities = listOf("High", "Medium", "Low")

    Box {
        OutlinedTextField(
            value = selectedPriority,
            onValueChange = {},
            label = { Text("Priority") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Priority")
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            priorities.forEach { priority ->
                DropdownMenuItem(
                    text = { Text(priority) },
                    onClick = {
                        onPrioritySelected(priority)
                        expanded = false
                    }
                )
            }
        }
    }
}
