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
fun StatusDropdown(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val statuses = listOf("To Do", "In Progress", "Done")

    Box {
        OutlinedTextField(
            value = selectedStatus,
            onValueChange = {},
            label = { Text("Status") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Status")
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            statuses.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status) },
                    onClick = {
                        onStatusSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}
