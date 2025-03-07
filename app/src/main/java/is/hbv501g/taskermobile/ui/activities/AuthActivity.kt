package `is`.hbv501g.taskermobile.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import `is`.hbv501g.taskermobile.R

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate a layout that contains a FragmentContainerView
        setContentView(R.layout.activity_auth)
    }
}