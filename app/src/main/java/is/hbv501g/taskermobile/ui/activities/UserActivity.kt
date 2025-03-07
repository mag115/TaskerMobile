package `is`.hbv501g.taskermobile.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import `is`.hbv501g.taskermobile.R

class UserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user) // e.g., contains a FrameLayout with id "fragment_container"
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UserFragment.newInstance())
                .commit()
        }
    }
}
