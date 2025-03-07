package `is`.hbv501g.taskermobile

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import `is`.hbv501g.taskermobile.ui.Routes
import `is`.hbv501g.taskermobile.ui.fragments.HomeFragment
import `is`.hbv501g.taskermobile.ui.fragments.LoginFragment
import `is`.hbv501g.taskermobile.ui.fragments.SignupFragment
import `is`.hbv501g.taskermobile.ui.fragments.WelcomeFragment

@Composable
fun AuthNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.WELCOME) {
        composable(Routes.WELCOME) { WelcomeFragment() }
        composable(Routes.LOGIN) { LoginFragment() }
        composable(Routes.SIGNUP) { SignupFragment() }
        composable(Routes.HOME) { HomeFragment() }
    }
}