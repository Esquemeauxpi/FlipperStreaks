package com.flipperstreaks.app.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.flipperstreaks.app.ui.account.AccountScreen
import com.flipperstreaks.app.ui.history.HistoryScreen
import com.flipperstreaks.app.ui.map.MapScreen
import com.flipperstreaks.app.ui.nearby.NearbyScreen
import com.flipperstreaks.app.ui.navigation.Routes
import com.flipperstreaks.app.ui.streaks.StreaksScreen

// Tab indices
// 0 Nearby  1 Map  2 Streaks  3 History  4 Account
@Composable
fun MainShell(
    navController: NavController,
    onLocationClick: (Int, String) -> Unit,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableStateOf(initialTab) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                    label = { Text("Nearby") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.Map, contentDescription = null) },
                    label = { Text("Map") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Filled.Whatshot, contentDescription = null) },
                    label = { Text("Streaks") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text("Account") }
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> NearbyScreen(
                onLocationClick = onLocationClick,
                modifier = Modifier.padding(innerPadding)
            )
            1 -> MapScreen(
                onLocationClick = onLocationClick,
                modifier = Modifier.padding(innerPadding)
            )
            2 -> StreaksScreen(modifier = Modifier.padding(innerPadding))
            3 -> HistoryScreen(modifier = Modifier.padding(innerPadding))
            4 -> AccountScreen(
                onConnectClick = { navController.navigate(Routes.LOGIN_LANDING) },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
