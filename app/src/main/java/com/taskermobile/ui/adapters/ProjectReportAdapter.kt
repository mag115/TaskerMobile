package com.taskermobile.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taskermobile.data.model.ProjectReport
import com.taskermobile.databinding.ItemProjectReportBinding

class ProjectReportAdapter(
    private var currentProjectName: String,
    private val onItemClicked: (ProjectReport) -> Unit
) : ListAdapter<ProjectReport, ProjectReportAdapter.ReportViewHolder>(ReportDiffCallback()) {

    fun setProjectName(newName: String) {
        currentProjectName = newName
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemProjectReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReportViewHolder(binding, currentProjectName, onItemClicked)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class ReportViewHolder(
        private val binding: ItemProjectReportBinding,
        private var currentProjectName: String,
        private val onItemClicked: (ProjectReport) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(report: ProjectReport, position: Int) {
            binding.apply {
                // Show project-specific report title using the current project name and sequence number.
                reportId.text = "Report for $currentProjectName #${position + 1}"
                reportDate.text = "Date: ${report.reportDate ?: "N/A"}"
                reportPerformance.text = "Performance: ${report.overallPerformance ?: "N/A"}"
                root.setOnClickListener {
                    onItemClicked(report)
                }
            }
        }
    }

    private class ReportDiffCallback : DiffUtil.ItemCallback<ProjectReport>() {
        override fun areItemsTheSame(oldItem: ProjectReport, newItem: ProjectReport): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: ProjectReport, newItem: ProjectReport): Boolean {
            return oldItem == newItem
        }
    }
}
