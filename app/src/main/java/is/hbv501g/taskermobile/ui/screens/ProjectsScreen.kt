package `is`.hbv501g.taskermobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import `is`.hbv501g.taskermobile.data.model.Project
import `is`.hbv501g.taskermobile.data.session.SessionManager
import `is`.hbv501g.taskermobile.ui.shared.AppHeader
import `is`.hbv501g.taskermobile.ui.shared.BottomNavBar
import `is`.hbv501g.taskermobile.ui.shared.ProjectCard
import `is`.hbv501g.taskermobile.ui.viewmodels.ProjectViewModel
import `is`.hbv501g.taskermobile.ui.viewmodels.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    navController: NavController,
    sessionManager: SessionManager,
    projectViewModel: ProjectViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    // Observe projects and current user from the viewmodels
    val projects by projectViewModel.projects.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()

    // Load projects when the screen is first composed
    LaunchedEffect(Unit) {
        projectViewModel.loadAllProjects()
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }
    var newProjectDescription by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AppHeader(
                title = "Projects",
                navController = navController,
                backButton = false,
                sessionManager = sessionManager
            )
        },
        bottomBar = { BottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Project")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Your Projects",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (projects.isEmpty()) {
                Text("No projects available")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(projects) { project ->
                        ProjectCard(project = project)
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Project") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newProjectName,
                        onValueChange = { newProjectName = it },
                        label = { Text("Project Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newProjectDescription,
                        onValueChange = { newProjectDescription = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProjectName.isNotBlank() && currentUser != null) {
                            // Create a new project using current user as owner
                            val newProject = Project(
                                id = null,
                                name = newProjectName,
                                description = newProjectDescription.ifBlank { null },
                                createdAt = null,
                                updatedAt = null,
                                tasks = emptyList(),
                                members = emptyList(),
                                owner = currentUser
                            )
                            coroutineScope.launch {
                                projectViewModel.createProject(newProject)
                                projectViewModel.loadAllProjects() // Refresh the list
                            }
                            showCreateDialog = false
                            newProjectName = ""
                            newProjectDescription = ""
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                Button(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}