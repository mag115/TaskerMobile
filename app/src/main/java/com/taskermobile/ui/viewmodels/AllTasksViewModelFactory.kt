import android.se.omapi.Session
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.session.SessionManager
import com.taskermobile.ui.viewmodels.AllTasksViewModel

class AllTasksViewModelFactory(
    private val taskRepository: TaskRepository,
    private val sessionManager: SessionManager

) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AllTasksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AllTasksViewModel(taskRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
