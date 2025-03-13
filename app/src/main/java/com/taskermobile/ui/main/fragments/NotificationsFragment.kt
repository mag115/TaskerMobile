package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskermobile.data.api.RetrofitClient
import com.taskermobile.data.local.TaskerDatabase
import com.taskermobile.data.repository.NotificationRepository
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.FragmentNotificationsBinding
import com.taskermobile.ui.adapters.NotificationAdapter
import com.taskermobile.ui.main.controllers.NotificationsController

class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationsController: NotificationsController
    private lateinit var adapter: NotificationAdapter

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

        binding.swipeRefresh.setOnRefreshListener { fetchNotifications() }
        binding.fabRefreshNotifications.setOnClickListener { fetchNotifications() }
    }

    private fun setupDependencies() {
        val sessionManager = SessionManager(requireContext())
        val database = TaskerDatabase.getDatabase(requireContext())
        val notificationDao = database.notificationDao()

        val notificationRepository = NotificationRepository(
            notificationApi = RetrofitClient.createService(sessionManager),
            notificationDao = notificationDao
        )

        notificationsController = NotificationsController(notificationRepository)
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
        binding.progressBar.visibility = View.VISIBLE
        binding.errorText.visibility = View.GONE

        notificationsController.fetchNotifications { notifications, unreadCount ->
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
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
