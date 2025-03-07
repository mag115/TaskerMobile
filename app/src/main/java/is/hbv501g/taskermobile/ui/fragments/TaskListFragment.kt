package `is`.hbv501g.taskermobile.ui.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import `is`.hbv501g.taskermobile.R
import `is`.hbv501g.taskermobile.data.model.Task
import `is`.hbv501g.taskermobile.ui.adapters.TaskAdapter
import `is`.hbv501g.taskermobile.ui.viewmodels.TaskViewModel

class TaskListFragment : Fragment() {

    private lateinit var taskRecyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskViewModel: TaskViewModel

    companion object {
        fun newInstance() = TaskListFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_task_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        taskRecyclerView = view.findViewById(R.id.recycler_view_tasks)
        taskRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        taskAdapter = TaskAdapter(emptyList()) { task -> onTaskSelected(task) }
        taskRecyclerView.adapter = taskAdapter

        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)
        taskViewModel.tasks.observe(viewLifecycleOwner, { tasks ->
            taskAdapter.updateTasks(tasks)
        })
    }

    private fun onTaskSelected(task: Task) {
        // TODO: Navigate to a detail fragment if needed
    }
}