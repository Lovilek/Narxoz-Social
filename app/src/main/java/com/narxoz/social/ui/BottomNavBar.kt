package com.narxoz.social.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    onScreenSelected: (String) -> Unit,
    unreadChats: Int = 0
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
                    contentDescription = stringResource(R.string.home),
                    modifier = navIconModifier
                )
            },
            label  = { Text(stringResource(R.string.home)) },
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
                    contentDescription = stringResource(R.string.events),
                    modifier = navIconModifier
                )
            },
            label  = { Text(stringResource(R.string.events)) },
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
                if (unreadChats > 0) {
                    BadgedBox(badge = { Badge { Text(unreadChats.toString()) } }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chat),
                            contentDescription = stringResource(R.string.chats),
                            modifier = navIconModifier
                        )
                    }
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_chat),
                        contentDescription = stringResource(R.string.chats),
                        modifier = navIconModifier
                    )
                }
            },
            label  = { Text(stringResource(R.string.chats)) },
            colors = itemColors()
        )

        NavigationBarItem(
            selected = currentScreen == "organizations",
            onClick  = { onScreenSelected("organizations") },
            icon = {
                Icon(
                    Icons.Filled.People,
                    contentDescription = stringResource(R.string.orgs),
                    modifier = navIconModifier
                )
            },
            label  = { Text(stringResource(R.string.orgs)) },
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
                    contentDescription = stringResource(R.string.settings),
                    modifier = navIconModifier
                )
            },
            label  = { Text(stringResource(R.string.settings)) },
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