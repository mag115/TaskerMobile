package com.taskermobile.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
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
        // ✅ Corrected `inflate()` to properly attach the view
        val rootView = LayoutInflater.from(context).inflate(
            com.taskermobile.R.layout.layout_app_toolbar, this, true
        )
        binding = LayoutAppToolbarBinding.bind(rootView) // ✅ Properly bind view
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
            binding.backButton.visibility = View.VISIBLE
            binding.backButton.setOnClickListener {
                onBackPressed?.invoke()
            }
        } else {
            binding.backButton.visibility = View.GONE
        }

        binding.logoutButton.setOnClickListener {
            lifecycleScope.launchWhenStarted {

                sessionManager.clearSession()
                

                val isBiometricEnabled = sessionManager.isBiometricLoginEnabled()
                val (encToken, encIv) = sessionManager.getEncryptedTokenAndIv()
                android.util.Log.d("AppToolbar", "Logout - Biometric state: enabled=$isBiometricEnabled, hasToken=${encToken != null}, hasIv=${encIv != null}")
                
                NavigationManager.navigateToAuth(context)
            }
        }
    }
}
