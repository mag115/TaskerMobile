package `is`.hbv501g.taskermobile.ui.main.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import `is`.hbv501g.taskermobile.controller.AuthController
import `is`.hbv501g.taskermobile.data.model.Task
import `is`.hbv501g.taskermobile.data.session.SessionManager
import `is`.hbv501g.taskermobile.ui.controllers.TaskController
import `is`.hbv501g.taskermobile.ui.shared.AppHeader
import `is`.hbv501g.taskermobile.ui.shared.BottomNavBar
import `is`.hbv501g.taskermobile.ui.shared.DatePickerButton
import `is`.hbv501g.taskermobile.ui.shared.PriorityDropdown
import `is`.hbv501g.taskermobile.ui.shared.StatusDropdown
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@Composable
fun CreateTask(
    navController: NavController,
    sessionManager: SessionManager,
) {
    val controller = remember { TaskController( sessionManager) }
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

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Default project ID value
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

            // Title input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title*") },
                modifier = Modifier.fillMaxWidth()
            )

            // Description input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Deadline picker
            DatePickerButton(
                selectedDate = deadline,
                onDateSelected = { deadline = it }
            )

            // Priority dropdown
            PriorityDropdown(
                selectedPriority = priority,
                onPrioritySelected = { priority = it }
            )

            // Status dropdown
            StatusDropdown(
                selectedStatus = status,
                onStatusSelected = { status = it }
            )

            // Estimated Duration input
            OutlinedTextField(
                value = estimatedDuration.toString(),
                onValueChange = { estimatedDuration = it.toDoubleOrNull() ?: 0.0 },
                label = { Text("Estimated Duration (in hours)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Effort Percentage input
            OutlinedTextField(
                value = effortPercentage.toString(),
                onValueChange = { effortPercentage = it.toDoubleOrNull() ?: 0.0 },
                label = { Text("Effort Percentage (%)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Dependency input
            OutlinedTextField(
                value = dependency,
                onValueChange = { dependency = it },
                label = { Text("Dependency (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Create Task Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Title is required"
                        return@Button
                    }
                    coroutineScope.launch {
                        isLoading = true
                        try {
                            // Create the Task using the repository/controller
                            controller.createTask(
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