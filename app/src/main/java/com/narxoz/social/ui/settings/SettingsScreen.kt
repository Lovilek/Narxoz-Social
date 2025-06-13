package com.narxoz.social.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.narxoz.social.ui.navigation.LocalNavController
import com.narxoz.social.repository.AuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onLogout: () -> Unit = {}) {
    Scaffold(topBar = { SmallTopAppBar(title = { Text("Settings") }) }) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Тема, уведомления, о приложении…", style = MaterialTheme.typography.bodyMedium)
            val rootNav = LocalNavController.current
            Button(onClick = { rootNav.navigate("addFriend") }) {
                Text("Добавить друга")
            }
            Button(onClick = {
                AuthRepository.forceLogout()
                onLogout()
            }) { Text("Выйти из аккаунта") }
        }
    }
}