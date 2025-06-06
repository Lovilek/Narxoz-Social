package com.narxoz.social.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.narxoz.social.R
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Нижняя панель навигации (Material 3 NavigationBar).
 */
@Composable
fun BottomNavBar(
    currentScreen: String,
    onScreenSelected: (String) -> Unit
) {
    val navIconModifier = Modifier.size(26.dp)   // ⬅︎ единый размер для всех

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == "home",
            onClick  = { onScreenSelected("home") },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_home),
                    contentDescription = "Home",
                    modifier = navIconModifier
                )
            },
            label  = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(          // ← готовые дефолты M3
                selectedIconColor   = MaterialTheme.colorScheme.primary,
                selectedTextColor   = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor      = MaterialTheme.colorScheme.secondaryContainer
            )
        )

        NavigationBarItem(
            selected = currentScreen == "events",
            onClick  = { onScreenSelected("events") },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_events),
                    contentDescription = "Events",
                    modifier = navIconModifier
                )
            },
            label  = { Text("Events") },
            colors = NavigationBarItemDefaults.colors(          // ← готовые дефолты M3
                selectedIconColor   = MaterialTheme.colorScheme.primary,
                selectedTextColor   = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor      = MaterialTheme.colorScheme.secondaryContainer
            )
        )

        NavigationBarItem(
            selected = currentScreen == "chats"
                    || currentScreen.startsWith("chat/"),
            onClick  = { onScreenSelected("chats") },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_chat),
                    contentDescription = "Chats",
                    modifier = navIconModifier
                )
            },
            label  = { Text("Chats") },
            colors = itemColors()
        )

        NavigationBarItem(
            selected = currentScreen == "organizations",
            onClick  = { onScreenSelected("organizations") },
            icon = {
                Icon(
                    Icons.Filled.People,
                    contentDescription = "Orgs",
                    modifier = navIconModifier
                )
            },
            label  = { Text("Orgs") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor   = MaterialTheme.colorScheme.primary,
                selectedTextColor   = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor      = MaterialTheme.colorScheme.secondaryContainer
            )
        )

        NavigationBarItem(
            selected = currentScreen == "settings",
            onClick  = { onScreenSelected("settings") },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = "Settings",
                    modifier = navIconModifier
                )
            },
            label  = { Text("Settings") },
            colors = NavigationBarItemDefaults.colors(          // ← готовые дефолты M3
                selectedIconColor   = MaterialTheme.colorScheme.primary,
                selectedTextColor   = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor      = MaterialTheme.colorScheme.secondaryContainer
            )
        )
    }
}

@Composable
private fun itemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor   = MaterialTheme.colorScheme.primary,
    selectedTextColor   = MaterialTheme.colorScheme.primary,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    indicatorColor      = Color(0xFFFFC0CB)        // фон‑индикатор под выбранной иконкой
)