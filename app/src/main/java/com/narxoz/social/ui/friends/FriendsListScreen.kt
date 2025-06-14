package com.narxoz.social.ui.friends

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.narxoz.social.ui.navigation.LocalNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsListScreen(
    onBack: () -> Unit = {},
    vm: FriendsListViewModel = viewModel(),
) {
    val state by vm.state.collectAsState()
    val navController = LocalNavController.current

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Мои друзья") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            ScrollableTabRow(selectedTabIndex = state.tab.ordinal) {
                Tab(
                    text = { Text("Друзья") },
                    selected = state.tab == FriendsTab.FRIENDS,
                    onClick = { vm.changeTab(FriendsTab.FRIENDS) }
                )
                Tab(
                    text = { Text("Входящие") },
                    selected = state.tab == FriendsTab.INCOMING,
                    onClick = { vm.changeTab(FriendsTab.INCOMING) }
                )
                Tab(
                    text = { Text("Исходящие") },
                    selected = state.tab == FriendsTab.OUTGOING,
                    onClick = { vm.changeTab(FriendsTab.OUTGOING) }
                )
            }
            OutlinedTextField(
                value = state.filter,
                onValueChange = vm::updateFilter,
                label = { Text("Фильтр") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                state.error != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text(state.error ?: "", color = MaterialTheme.colorScheme.error) }

                else -> {
                    when (state.tab) {
                        FriendsTab.FRIENDS -> {
                            val friends = state.friends.filter {
                                it.nickname?.contains(state.filter, true) == true ||
                                        it.fullName?.contains(state.filter, true) == true
                            }
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(friends) { friend ->
                                    ListItem(
                                        headlineContent = {
                                            Text(friend.nickname ?: friend.fullName ?: "ID ${friend.id}")
                                        },
                                        trailingContent = {
                                            IconButton(onClick = { vm.removeFriend(friend.id) }) {
                                                Icon(Icons.Default.Delete, null)
                                            }
                                        },
                                        modifier = Modifier.clickable {
                                            navController.navigate("user/${friend.id}")
                                        }
                                    )
                                    Divider()
                                }
                            }
                        }

                        FriendsTab.INCOMING -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.incoming) { req ->
                                    val user = req.fromUser
                                    ListItem(
                                        headlineContent = {
                                            Text(user?.nickname ?: user?.fullName ?: "ID ${user?.id}")
                                        }
                                    )
                                    Divider()
                                }
                            }
                        }

                        FriendsTab.OUTGOING -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.outgoing) { req ->
                                    val user = req.toUser
                                    ListItem(
                                        headlineContent = {
                                            Text(user?.nickname ?: user?.fullName ?: "ID ${user?.id}")
                                        },
                                        trailingContent = {
                                            IconButton(onClick = { vm.cancelRequest(req.id) }) {
                                                Icon(Icons.Default.Cancel, null)
                                            }
                                        }
                                    )
                                    Divider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}