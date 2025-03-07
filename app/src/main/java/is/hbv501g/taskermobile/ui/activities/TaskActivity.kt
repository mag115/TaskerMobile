package `is`.hbv501g.taskermobile.ui.activities


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import `is`.hbv501g.taskermobile.R
import `is`.hbv501g.taskermobile.ui.fragments.TaskListFragment

class TaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TaskListFragment.newInstance())
                .commit()
        }
    }
}