package `is`.hbv501g.taskermobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import `is`.hbv501g.taskermobile.data.model.Task
import `is`.hbv501g.taskermobile.data.session.SessionManager
import `is`.hbv501g.taskermobile.ui.shared.AppHeader
import `is`.hbv501g.taskermobile.ui.shared.BottomNavBar
import `is`.hbv501g.taskermobile.ui.shared.DatePickerButton
import `is`.hbv501g.taskermobile.ui.shared.PriorityDropdown
import `is`.hbv501g.taskermobile.ui.shared.StatusDropdown
import `is`.hbv501g.taskermobile.ui.viewmodels.TaskViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@Composable
fun CreateTask(
    navController: NavController,
    sessionManager: SessionManager,
    taskViewModel: TaskViewModel = viewModel() // using the default ViewModel provider
) {
    val context = LocalContext.current
    // Local UI state variables
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var deadline by rememberSaveable { mutableStateOf<LocalDateTime?>(null) }
    var priority by rememberSaveable { mutableStateOf("Medium") }
    var status by rememberSaveable { mutableStateOf("To Do") }
    var estimatedDuration by rememberSaveable { mutableStateOf(0.0) }
    var effortPercentage by rememberSaveable { mutableStateOf(0.0) }
    var dependency by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val defaultProjectId: Long = 1L

    Scaffold(
        topBar = { AppHeader(title = "Create Task", navController = navController, backButton = true, sessionManager) },
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title*") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            DatePickerButton(
                selectedDate = deadline,
                onDateSelected = { deadline = it }
            )
            PriorityDropdown(
                selectedPriority = priority,
                onPrioritySelected = { priority = it }
            )
            StatusDropdown(
                selectedStatus = status,
                onStatusSelected = { status = it }
            )
            OutlinedTextField(
                value = estimatedDuration.toString(),
                onValueChange = { estimatedDuration = it.toDoubleOrNull() ?: 0.0 },
                label = { Text("Estimated Duration (in hours)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = effortPercentage.toString(),
                onValueChange = { effortPercentage = it.toDoubleOrNull() ?: 0.0 },
                label = { Text("Effort Percentage") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = dependency,
                onValueChange = { dependency = it },
                label = { Text("Dependency") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        try {
                            // Call createTask on the TaskViewModel instead of a controller
                            taskViewModel.createTask(
                                Task(
                                    title = title,
                                    description = description,
                                    deadline = deadline?.format(DateTimeFormatter.ISO_DATE_TIME),
                                    priority = priority,
                                    status = status,
                                    estimatedDuration = estimatedDuration,
                                    effortPercentage = effortPercentage.toFloat(),
                                    dependency = dependency,
                                    projectId = defaultProjectId
                                )
                            )
                            navController.popBackStack()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to create task"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Create Task")
                }
            }
        }
    }
}
