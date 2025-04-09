package com.taskermobile.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.taskermobile.databinding.LayoutAppToolbarBinding

class AppToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Toolbar(context, attrs, defStyleAttr) {

    private val binding: LayoutAppToolbarBinding

    init {
        val rootView = LayoutInflater.from(context).inflate(
            com.taskermobile.R.layout.layout_app_toolbar, this, true
        )
        binding = LayoutAppToolbarBinding.bind(rootView) // ✅ Properly bind view
    }

    fun setup(
        title: String? = null,
        showBackButton: Boolean = true,
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
    }
}
