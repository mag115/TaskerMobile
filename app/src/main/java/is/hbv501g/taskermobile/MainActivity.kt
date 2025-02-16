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
import `is`.hbv501g.taskermobile.data.api.RetrofitClient
import `is`.hbv501g.taskermobile.data.dataStore
import `is`.hbv501g.taskermobile.data.repository.AuthRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataStore = applicationContext.dataStore
        RetrofitClient.checkBackendConnection()
        // Pass this instance to your repository:
        val authRepository = AuthRepository(dataStore, RetrofitClient.authApiService)
        setContent {
            TaskerMobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    TaskerMobileTheme {
        AppNavGraph()
    }
}