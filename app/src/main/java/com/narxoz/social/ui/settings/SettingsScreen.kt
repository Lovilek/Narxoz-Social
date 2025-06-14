package com.narxoz.social.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.narxoz.social.R
import com.narxoz.social.repository.AuthRepository
import com.narxoz.social.ui.navigation.LocalNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onLogout: () -> Unit = {}, vm: SettingsViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val rootNav = LocalNavController.current

    val langs = mapOf(
        "en" to stringResource(R.string.lang_en),
        "ru" to stringResource(R.string.lang_ru),
        "kk" to stringResource(R.string.lang_kk)
    )
    var langExpanded by remember { mutableStateOf(false) }

    Scaffold(topBar = { SmallTopAppBar(title = { Text(stringResource(R.string.settings_title)) }) }) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                stringResource(R.string.settings_hint),
                style = MaterialTheme.typography.bodyMedium
            )

            ExposedDropdownMenuBox(
                expanded = langExpanded,
                onExpandedChange = { langExpanded = !langExpanded }) {
                TextField(
                    readOnly = true,
                    value = langs[state.language] ?: state.language,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.settings_language)) },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = langExpanded,
                    onDismissRequest = { langExpanded = false }) {
                    langs.forEach { (code, title) ->
                        DropdownMenuItem(
                            text = { Text(title) },
                            onClick = { vm.setLanguage(code); langExpanded = false }
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.settings_notifications),
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = state.notifications, onCheckedChange = vm::setNotifications)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.settings_privacy), modifier = Modifier.weight(1f))
                Switch(checked = state.isPrivate, onCheckedChange = vm::setPrivate)
            }

            Button(onClick = { rootNav.navigate("friends") }) { Text(stringResource(R.string.my_friends)) }
            Button(onClick = { rootNav.navigate("addFriend") }) { Text(stringResource(R.string.add_friend)) }
        }
        Button(onClick = {
            AuthRepository.forceLogout()
            onLogout()
        }) { Text(stringResource(R.string.logout)) }
    }
}