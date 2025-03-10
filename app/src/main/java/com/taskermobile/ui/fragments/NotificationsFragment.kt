package com.taskermobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskermobile.databinding.FragmentNotificationsBinding
import com.taskermobile.ui.adapters.NotificationAdapter
import com.taskermobile.ui.viewmodels.NotificationsViewModel

class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    // ✅ Use viewModels() for ViewModel initialization
    private val viewModel: NotificationsViewModel by viewModels()

    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Setup RecyclerView
        adapter = NotificationAdapter { notification ->
            viewModel.markAsRead(notification) // ✅ Click to mark as read
        }
        binding.notificationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationsRecyclerView.adapter = adapter

        // ✅ Observe LiveData properly
        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
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

        // ✅ Pull to Refresh
        binding.swipeRefresh.setOnRefreshListener {
            refreshNotifications()
        }

        // ✅ FAB Click to Refresh
        binding.fabRefreshNotifications.setOnClickListener {
            refreshNotifications()
        }
    }

    private fun refreshNotifications() {
        binding.progressBar.visibility = View.VISIBLE
        binding.errorText.visibility = View.GONE
        viewModel.fetchNotifications() // ✅ Now correctly referenced
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
