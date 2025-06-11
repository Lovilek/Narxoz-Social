package com.narxoz.social.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.narxoz.social.R

/**
 * Верхняя панель.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainFeedTopBar(
    onToggleTheme: () -> Unit,
    notifications: Int,
    onNotifications: () -> Unit
) {
    TopAppBar(
        title = { Text("Narxoz Social") },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        actions = {
            IconButton(onClick = onToggleTheme) {
                val isDark = isSystemInDarkTheme()    // показываем текущий
                val icon   = if (isDark) Icons.Filled.WbSunny else Icons.Filled.DarkMode
                Icon(icon, contentDescription = "Toggle theme")
            }
            val topIconModifier = Modifier.size(28.dp)
            IconButton(onClick = { /* TODO: Переход в профиль */ }) {
                Icon(
                    painter = painterResource(R.drawable.ic_profile),
                    contentDescription = "Profile",
                    modifier = topIconModifier
                )
            }
            IconButton(onClick = { /* TODO: Календарь/События */ }) {
                Icon(
                    painter = painterResource(R.drawable.ic_calendar),
                    contentDescription = "Calendar",
                    modifier = topIconModifier
                )
            }
            IconButton(onClick = onNotifications) {
                BadgedBox(badge = {
                    if (notifications > 0) Badge { Text(notifications.toString()) }
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_notifications),
                        contentDescription = "Notifications",
                        modifier = topIconModifier
                    )
                }
            }
        }
    )
}