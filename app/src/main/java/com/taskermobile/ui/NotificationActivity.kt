package com.taskermobile.ui


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.taskermobile.R
import com.taskermobile.data.service.NotificationApiService
import com.taskermobile.ui.adapters.NotificationAdapter
import com.taskermobile.data.repository.NotificationRepository
import com.taskermobile.data.session.SessionManager
import com.taskermobile.data.api.RetrofitClient
import com.taskermobile.ui.viewmodels.NotificationViewModel
import com.taskermobile.ui.viewmodels.NotificationViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationActivity : AppCompatActivity() {

    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var adapter: NotificationAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        sessionManager = SessionManager(applicationContext)
        recyclerView = findViewById(R.id.notificationRecyclerView)
        adapter = NotificationAdapter(emptyList()) { notification ->
            // Mark notification as read when clicked
            notificationViewModel.markNotificationAsRead(notification.id)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Create NotificationApiService and repository using RetrofitClient and SessionManager
        val notificationApi = RetrofitClient.createService<NotificationApiService>(sessionManager)
        val repository = NotificationRepository(notificationApi)
        val factory = NotificationViewModelFactory(repository)
        notificationViewModel = ViewModelProvider(this, factory)
            .get(NotificationViewModel::class.java)

        // Load notifications once we have the user ID from SessionManager.
        lifecycleScope.launch {
            val userId = sessionManager.userId.first() ?: return@launch
            notificationViewModel.loadNotifications(userId)
        }

        // Observe notifications and update the RecyclerView adapter.
        lifecycleScope.launchWhenStarted {
            notificationViewModel.notifications.observe(this@NotificationActivity) { notifications ->
                adapter.updateData(notifications)
            }
        }
    }
}