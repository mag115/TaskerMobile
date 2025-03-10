package com.taskermobile.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskermobile.data.local.entity.NotificationEntity
import com.taskermobile.data.repository.NotificationRepository
import kotlinx.coroutines.launch

class NotificationsViewModel(private val notificationRepository: NotificationRepository) : ViewModel() {

    private val _notifications = MutableLiveData<List<NotificationEntity>>()
    val notifications: LiveData<List<NotificationEntity>> get() = _notifications

    private val _unreadCount = MutableLiveData<Int>()
    val unreadCount: LiveData<Int> get() = _unreadCount

    init {
        fetchNotifications() // ✅ Auto-fetch when ViewModel is created
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            val result = notificationRepository.getLocalNotifications()
            result.collect { notifications ->
                _notifications.postValue(notifications)
                _unreadCount.postValue(notifications.count { !it.isRead }) // Count unread
            }
        }
    }

    fun markAsRead(notification: NotificationEntity) {
        viewModelScope.launch {
            notificationRepository.markNotificationAsRead(notification.id)
            fetchNotifications() // ✅ Refresh list after marking as read
        }
    }
}

