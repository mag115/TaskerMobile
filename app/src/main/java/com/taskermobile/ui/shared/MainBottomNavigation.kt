package com.taskermobile.ui.shared

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.taskermobile.R

class MainBottomNavigation @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BottomNavigationView(context, attrs, defStyleAttr) {

    init {
        inflateMenu(R.menu.bottom_nav_menu)
    }

    fun setNavigationChangeListener(onItemSelected: (Int) -> Unit) {
        setOnItemSelectedListener { item ->
            onItemSelected(item.itemId)
            true
        }
    }
} 