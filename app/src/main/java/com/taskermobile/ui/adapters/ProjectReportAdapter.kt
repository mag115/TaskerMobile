package com.taskermobile.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taskermobile.data.model.ProjectReport
import com.taskermobile.databinding.ItemProjectReportBinding

class ProjectReportAdapter(
    private val onItemClicked: (ProjectReport) -> Unit
) : ListAdapter<ProjectReport, ProjectReportAdapter.ReportViewHolder>(ReportDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemProjectReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReportViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReportViewHolder(
        private val binding: ItemProjectReportBinding,
        private val onItemClicked: (ProjectReport) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(report: ProjectReport) {
            binding.apply {
                reportId.text = "Report #${report.id}"
                reportDate.text = "Date: ${report.reportDate ?: "N/A"}"
                reportPerformance.text = "Performance: ${report.overallPerformance ?: "N/A"}"
                // Instead of exporting the PDF, simply call onItemClicked(report)
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
