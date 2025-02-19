package `is`.hbv501g.taskermobile.ui.shared

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/**
 * Data class for navigation items
 */
data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

/**
 * Bottom Navigation Bar Component
 */
@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, "home"),
        BottomNavItem("Tasks", Icons.Outlined.Add, "create-task"),
        BottomNavItem("Settings", Icons.Default.Person, "Settings")
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route)
                    }
                }
            )
        }
    }
}
