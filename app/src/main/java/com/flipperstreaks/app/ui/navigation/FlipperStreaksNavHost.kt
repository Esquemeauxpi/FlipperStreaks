package com.flipperstreaks.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.flipperstreaks.app.ui.locationdetail.LocationDetailScreen
import com.flipperstreaks.app.ui.login.LoginFormScreen
import com.flipperstreaks.app.ui.login.LoginLandingScreen
import com.flipperstreaks.app.ui.main.MainShell
import com.flipperstreaks.app.ui.onboarding.OnboardingScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val MAIN = "main?tab={tab}"
    const val LOCATION_DETAIL = "location/{locationId}/{locationName}"
    const val LOGIN_LANDING = "login_landing"
    const val LOGIN_FORM = "login_form"

    fun main(tab: Int = 0) = "main?tab=$tab"
    fun locationDetail(id: Int, name: String) = "location/$id/${name.encodeForRoute()}"

    private fun String.encodeForRoute() = java.net.URLEncoder.encode(this, "UTF-8")
}

@Composable
fun FlipperStreaksNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.ONBOARDING) {

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onContinueToNearby = {
                    navController.navigate(Routes.main(tab = 0)) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                onContinueToSearch = {
                    navController.navigate(Routes.main(tab = 1)) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.MAIN,
            arguments = listOf(
                navArgument("tab") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getInt("tab") ?: 0
            MainShell(
                navController = navController,
                initialTab = initialTab,
                onLocationClick = { id, name ->
                    navController.navigate(Routes.locationDetail(id, name))
                }
            )
        }

        composable(
            route = Routes.LOCATION_DETAIL,
            arguments = listOf(
                navArgument("locationId") { type = NavType.IntType },
                navArgument("locationName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val locationId = backStackEntry.arguments?.getInt("locationId") ?: 0
            val locationName = backStackEntry.arguments?.getString("locationName")
                ?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: ""
            LocationDetailScreen(
                locationId = locationId,
                locationName = locationName,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.LOGIN_LANDING) {
            LoginLandingScreen(
                onLoginClick = { navController.navigate(Routes.LOGIN_FORM) },
                onDismiss = { navController.popBackStack() }
            )
        }

        composable(Routes.LOGIN_FORM) {
            LoginFormScreen(
                onLoginSuccess = {
                    navController.popBackStack(Routes.MAIN, inclusive = false)
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
