package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskermobile.TaskerApplication
import com.taskermobile.data.model.ProjectReport
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.FragmentReportDetailBinding
import com.taskermobile.ui.adapters.TaskAdapter
import com.taskermobile.ui.main.controllers.ProjectReportController
import com.taskermobile.util.generatePdf
import kotlinx.coroutines.launch
import java.io.File

class ReportDetailFragment : Fragment() {

    private var _binding: FragmentReportDetailBinding? = null
    private val binding get() = _binding!!
    private var currentReport: ProjectReport? = null

    // Retrieve reportId via Safe Args
    private val args: ReportDetailFragmentArgs by navArgs()

    private lateinit var reportController: ProjectReportController
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDependencies()
        setupRecyclerView()
        loadReportDetails(args.reportId)
        setupDownloadButton()
    }

    private fun setupDependencies() {
        val sessionManager = SessionManager(requireContext())
        val db = (requireActivity().application as TaskerApplication).database
        val reportDao = db.projectReportDao()
        val taskDao = db.taskDao()
        reportController = ProjectReportController(sessionManager, reportDao, taskDao)
    }

    private fun setupRecyclerView() {
        // Now pass an additional lambda for onCommentSend (even if it's a no-op)
        taskAdapter = TaskAdapter(
            taskActions = reportController, // if your TaskAdapter supports actions
            onTaskClick = { /* Handle task click if needed */ },
            onCommentSend = { task, comment ->
                // For now, simply ignore or log the comment.
            }
        )
        binding.tasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }

    private fun loadReportDetails(reportId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val report: ProjectReport? = reportController.loadReportDetails(reportId)
                if (report != null) {
                    bindReportToUI(report)
                } else {
                    Toast.makeText(requireContext(), "No report found for ID $reportId", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun bindReportToUI(report: ProjectReport) {
        currentReport = report
        binding.apply {
            reportIdText.text = "Report #${report.id}"
            reportDateText.text = "Date: ${report.reportDate ?: "N/A"}"
        }
        taskAdapter.submitList(report.tasks)
    }

    private fun setupDownloadButton() {
        binding.downloadButton.setOnClickListener {
            // Ensure the report is loaded
            val report = currentReport
            if (report != null) {
                // Generate PDF using our local utility function
                val pdfBytes = generatePdf(report)
                val savedFile = savePdfToFile(report.id ?: 0L, pdfBytes)
                if (savedFile != null) {
                    Toast.makeText(requireContext(), "Report downloaded to: ${savedFile.absolutePath}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to save report file.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Report not loaded yet.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun savePdfToFile(reportId: Long, pdfBytes: ByteArray): File? {
        return try {
            val downloadsDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "project_report_$reportId.pdf")
            file.writeBytes(pdfBytes)
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
