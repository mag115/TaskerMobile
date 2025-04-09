package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskermobile.data.api.RetroFitClient
import com.taskermobile.data.local.TaskerDatabase
import com.taskermobile.data.repository.NotificationRepository
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.FragmentNotificationsBinding
import com.taskermobile.ui.adapters.NotificationAdapter
import com.taskermobile.ui.main.controllers.NotificationsController
import kotlinx.coroutines.launch
import android.app.Application
import com.taskermobile.TaskerApplication
import com.taskermobile.data.api.NotificationApiService
import com.taskermobile.data.local.entity.NotificationEntity

class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationsController: NotificationsController
    private lateinit var adapter: NotificationAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var application: Application

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDependencies()
        setupRecyclerView()
        fetchNotifications()

        binding.swipeRefresh.setOnRefreshListener { refreshNotifications() }
        binding.fabRefreshNotifications.setOnClickListener { refreshNotifications() }
    }

    private fun setupDependencies() {
        sessionManager = SessionManager(requireContext())
        val application = requireActivity().application
        val database = (application as TaskerApplication).database
        val notificationDao = database.notificationDao()
        val notificationApiService = RetroFitClient.createService<NotificationApiService>(application, sessionManager)
        val notificationRepository = NotificationRepository(notificationApiService, notificationDao)

        notificationsController = NotificationsController(notificationRepository, sessionManager)
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter { notification ->
            notificationsController.markAsRead(notification) {
                fetchNotifications() // Refresh UI after marking as read
            }
        }
        binding.notificationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationsRecyclerView.adapter = adapter
    }

    private fun fetchNotifications() {
        _binding?.apply {
            progressBar.visibility = View.VISIBLE
            errorText.visibility = View.GONE
        }

        notificationsController.fetchNotifications { notifications, unreadCount ->
            updateUI(notifications, unreadCount)
        }
    }

    private fun refreshNotifications() {
        _binding?.apply {
            progressBar.visibility = View.VISIBLE
            errorText.visibility = View.GONE
        }

        notificationsController.refreshNotifications { notifications, unreadCount ->
            updateUI(notifications, unreadCount)
        }
    }

    private fun updateUI(notifications: List<NotificationEntity>, unreadCount: Int) {
        val binding = _binding ?: return

        binding.progressBar.visibility = View.GONE
        binding.swipeRefresh.isRefreshing = false

        if (notifications.isEmpty()) {
            binding.errorText.visibility = View.VISIBLE
            binding.notificationsRecyclerView.visibility = View.GONE
        } else {
            binding.errorText.visibility = View.GONE
            binding.notificationsRecyclerView.visibility = View.VISIBLE
            adapter.submitList(notifications)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
