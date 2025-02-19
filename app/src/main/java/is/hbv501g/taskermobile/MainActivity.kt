package `is`.hbv501g.taskermobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import `is`.hbv501g.taskermobile.ui.theme.TaskerMobileTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import `is`.hbv501g.taskermobile.data.api.RetrofitClient
import `is`.hbv501g.taskermobile.AuthNavGraph
import `is`.hbv501g.taskermobile.data.dataStore
import `is`.hbv501g.taskermobile.data.repository.AuthRepository
import `is`.hbv501g.taskermobile.data.session.SessionManager
import `is`.hbv501g.taskermobile.navigation.MainNavGraph
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sessionManager = SessionManager(applicationContext)

        sessionManager.validateToken
        // Check if user is authenticated
        val isAuthenticated = runBlocking { sessionManager.authState.first() }

        setContent {
            TaskerMobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isAuthenticated) {
                        MainNavGraph(sessionManager)
                    } else {
                        AuthNavGraph(sessionManager)
                    }
                }
            }
        }
    }
}
