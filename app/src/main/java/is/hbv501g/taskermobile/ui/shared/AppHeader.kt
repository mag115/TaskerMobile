package `is`.hbv501g.taskermobile.ui.shared

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import `is`.hbv501g.taskermobile.data.session.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    title: String? = null,
    navController: NavController,
    backButton: Boolean = true,
    sessionManager: SessionManager,
) {
    val coroutineScope = rememberCoroutineScope()
    CenterAlignedTopAppBar(
        title = {
            title?.let { Text(text = it) }
        },
        navigationIcon = {
            if (backButton) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            TextButton(onClick = {
                coroutineScope.launch {
                    sessionManager.clearSession()
                    Log.d("AuthController", "User logged out")
                }
            }) {
                Text("Logout")
            }
        }
    )
}
