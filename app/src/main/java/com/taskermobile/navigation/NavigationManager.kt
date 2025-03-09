package com.taskermobile.navigation

import android.content.Context
import android.content.Intent
import com.taskermobile.MainActivity
import com.taskermobile.ui.auth.AuthActivity

class NavigationManager {
    companion object {
        fun navigateToAuth(context: Context) {
            val intent = Intent(context, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }

        fun navigateToMain(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }
} 