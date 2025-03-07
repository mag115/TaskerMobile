package `is`.hbv501g.taskermobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import `is`.hbv501g.taskermobile.data.session.SessionManager
import `is`.hbv501g.taskermobile.navigation.AppNavigation
import `is`.hbv501g.taskermobile.ui.theme.TaskerMobileTheme
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity() {

    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(applicationContext)
        setContentView(R.layout.activity_main)
    }

}