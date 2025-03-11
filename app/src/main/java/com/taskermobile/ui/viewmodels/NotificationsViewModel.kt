package com.taskermobile.ui.viewmodels

import androidx.lifecycle.*
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
            notificationRepository.getLocalNotifications().collect { notifications ->
                _notifications.postValue(notifications)
                _unreadCount.postValue(notifications.count { !it.isRead }) // ✅ Count unread
            }
        }
    }

    fun markAsRead(notification: NotificationEntity) {
        viewModelScope.launch {
            notificationRepository.markNotificationAsRead(notification.id)
            fetchNotifications() // ✅ Refresh list after marking as read
        }
    }

    // ✅ Add a Factory for dependency injection
    class Factory(private val repository: NotificationRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NotificationsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
