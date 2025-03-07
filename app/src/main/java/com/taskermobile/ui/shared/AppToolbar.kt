package com.taskermobile.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LifecycleCoroutineScope
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.LayoutAppToolbarBinding
import com.taskermobile.navigation.NavigationManager

class AppToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Toolbar(context, attrs, defStyleAttr) {

    private val binding: LayoutAppToolbarBinding

    init {
        binding = LayoutAppToolbarBinding.inflate(LayoutInflater.from(context), this)
    }

    fun setup(
        title: String? = null,
        showBackButton: Boolean = true,
        sessionManager: SessionManager,
        lifecycleScope: LifecycleCoroutineScope,
        onBackPressed: (() -> Unit)? = null
    ) {
        title?.let { binding.toolbarTitle.text = it }

        if (showBackButton) {
            binding.backButton.setOnClickListener {
                onBackPressed?.invoke()
            }
        } else {
            binding.backButton.visibility = GONE
        }

        binding.logoutButton.setOnClickListener {
            lifecycleScope.launchWhenStarted {
                sessionManager.clearSession()
                NavigationManager.navigateToAuth(context)
            }
        }
    }
} 