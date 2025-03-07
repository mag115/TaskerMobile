package `is`.hbv501g.taskermobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import `is`.hbv501g.taskermobile.R
import `is`.hbv501g.taskermobile.data.model.Task
import `is`.hbv501g.taskermobile.ui.viewmodels.TaskViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CreateTaskFragment : Fragment() {

    companion object {
        fun newInstance() = CreateTaskFragment()
    }

    private lateinit var taskViewModel: TaskViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

        val titleEditText: EditText = view.findViewById(R.id.editText_title)
        val descriptionEditText: EditText = view.findViewById(R.id.editText_description)
        val deadlineEditText: EditText = view.findViewById(R.id.editText_deadline)
        val prioritySpinner: Spinner = view.findViewById(R.id.spinner_priority)
        val statusSpinner: Spinner = view.findViewById(R.id.spinner_status)
        val estimatedDurationEditText: EditText = view.findViewById(R.id.editText_estimated_duration)
        val effortPercentageEditText: EditText = view.findViewById(R.id.editText_effort_percentage)
        val dependencyEditText: EditText = view.findViewById(R.id.editText_dependency)
        val createButton: Button = view.findViewById(R.id.button_create_task)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)

        // Set up spinners (assume you have defined string-array resources for priorities and statuses)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.priority_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            prioritySpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.status_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            statusSpinner.adapter = adapter
        }

        createButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            val title = titleEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val deadlineStr = deadlineEditText.text.toString()
            val deadline = if (deadlineStr.isNotEmpty())
                LocalDateTime.parse(deadlineStr, DateTimeFormatter.ISO_DATE_TIME)
            else null
            val priority = prioritySpinner.selectedItem.toString()
            val status = statusSpinner.selectedItem.toString()
            val estimatedDuration = estimatedDurationEditText.text.toString().toDoubleOrNull() ?: 0.0
            val effortPercentage = effortPercentageEditText.text.toString().toFloatOrNull() ?: 0f
            val dependency = dependencyEditText.text.toString()

            val newTask = Task(
                title = title,
                description = description,
                deadline = deadline?.format(DateTimeFormatter.ISO_DATE_TIME),
                priority = priority,
                status = status,
                estimatedDuration = estimatedDuration,
                effortPercentage = effortPercentage,
                dependency = dependency,
                projectId = 1L // Default project id; adjust as needed
            )

            // Create the task using the ViewModel
            taskViewModel.createTask(newTask)
            Toast.makeText(requireContext(), "Task created", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE

            // Optionally, navigate back to the task list
            parentFragmentManager.popBackStack()
        }
    }
}