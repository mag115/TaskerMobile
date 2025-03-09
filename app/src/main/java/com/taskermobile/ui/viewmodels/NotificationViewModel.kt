package com.taskermobile.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskermobile.data.model.NotificationResponse
import com.taskermobile.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class NotificationViewModel(private val repository: NotificationRepository) : ViewModel() {

    private val _notifications = MutableLiveData<List<NotificationResponse>>()
    val notifications: LiveData<List<NotificationResponse>> = _notifications

    fun loadNotifications(userId: Long) {
        viewModelScope.launch {
            try {
                val notificationsList = repository.fetchNotifications(userId)
                _notifications.value = notificationsList
            } catch (e: Exception) {
                // Handle errors appropriately
            }
        }
    }

    fun markNotificationAsRead(notificationId: Long) {
        viewModelScope.launch {
            repository.markAsRead(notificationId)
            // Optionally, update the LiveData after marking as read.
            _notifications.value = _notifications.value?.filter { it.id != notificationId }
        }
    }
}