package com.narxoz.social.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.narxoz.social.ui.chat.ChatScreen
import com.narxoz.social.ui.events.EventsScreen
import com.narxoz.social.ui.navigation.LocalNavController
import com.narxoz.social.ui.orgs.OrganizationsScreen
import com.narxoz.social.ui.settings.SettingsScreen
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import androidx.compose.material.pullrefresh.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.narxoz.social.ui.chat.ChatListScreen

/* -------------------------------------------------------------------------- */
/*                            PUBLIC COMPOSABLE                               */
/* -------------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainFeedScreen(
    vm: FeedViewModel = viewModel(),         // нужен лишь для «home»
    onToggleTheme: () -> Unit
) {
    /* внутренний NavController – управляет вкладками bottom-bar */
    val innerNav = rememberNavController()

    /* текущий route для подсветки иконки */
    val currentBack by innerNav.currentBackStackEntryAsState()
    val route = currentBack?.destination?.route
    val currentRoute = innerNav.currentBackStackEntryAsState().value?.destination?.route
    val currentTab = when {
        route?.startsWith("chat/") == true -> "chats"
        else                               -> route ?: "home"
    }

    /* -------- Scaffold с единственным BottomBar -------- */
    Scaffold(
        topBar = { MainFeedTopBar(onToggleTheme) },
        bottomBar = {
            BottomNavBar(
                currentScreen = currentTab,
                onScreenSelected = { route ->
                    if (route != currentTab) {
                        innerNav.navigate(route) {
                            launchSingleTop = true
                            restoreState    = true
                            popUpTo(innerNav.graph.startDestinationId) {
                                saveState = true
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->

        /* ---------- Внутренний NavHost ---------- */
        NavHost(
            navController = innerNav,
            startDestination = "home",
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            /* ---------- Home / лента ---------- */
            composable("home") {
                val state by vm.state.collectAsState()

                when {
                    state.isLoading && state.posts.isEmpty() ->
                        LoadingPlaceholder()

                    state.error != null && state.posts.isEmpty() ->
                        ErrorPlaceholder(state.error ?: "Неизвестная ошибка", vm::retry)

                    else -> FeedContent(
                        state    = state,
                        vm       = vm,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            /* ---------- События ---------- */
            composable("events") { EventsScreen() }

            /* ---------- Чаты ---------- */
            composable("chats") { ChatListScreen(innerNav) }

            composable(
                route = "chat/{chatId}",
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) {
                ChatScreen()           // hiltViewModel цепляет chatId из аргументов
            }

            /* ---------- Организации ---------- */
            composable("organizations") { OrganizationsScreen() }

            /* ---------- Настройки ---------- */
            composable("settings") {
                /* Можно безопасно вызвать здесь: мы ещё в композиции */
                val rootNav = LocalNavController.current

                SettingsScreen(
                    onLogout = {
                        rootNav.navigate("login") { popUpTo(0) }
                    }
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/*                         INTERNAL FEED COMPOSITION                          */
/* -------------------------------------------------------------------------- */

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FeedContent(
    state: FeedState,
    vm:    FeedViewModel,
    modifier: Modifier = Modifier
) {
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    /* -------- PULL-TO-REFRESH -------- */
    val pullState = rememberPullRefreshState(
        refreshing = state.isLoading,
        onRefresh  = { vm.refresh() }
    )

    /* ---------- подгрузка страниц ---------- */
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .filterNotNull()
            .map { it >= state.posts.lastIndex }
            .collect { atEnd ->
                if (atEnd && !state.isLoading) vm.loadNext()
            }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullState)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize(),
            contentPadding      = PaddingValues(top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            /* ---------- Top Clubs ---------- */
            if (state.topClubs.isNotEmpty()) {
                item { SectionTitle("Top Clubs") }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        items(state.topClubs) { club -> ClubAvatarItemRemote(club) }
                    }
                }
            }

            /* ---------- Horizontal Clubs ---------- */
            if (state.horizontalClubs.isNotEmpty()) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        items(state.horizontalClubs) { club -> ClubCardItemRemote(club) }
                    }
                }
            }

            /* ---------- Posts ---------- */
            items(state.posts) { p ->
                PostCard(
                    post    = p,
                    onLike  = { id -> vm.toggleLike(id) },
                    onShare = vm::share
                )
            }

            /* ---------- Футер-лоадер / ошибка дозагрузки ---------- */
            if (state.isLoading && state.posts.isNotEmpty()) {
                item { FooterLoader() }
            }
            if (state.error != null && state.posts.isNotEmpty()) {
                item { ErrorPlaceholder(state.error, vm::loadNext) }
            }
        }

        /* -------- Индикатор «кругляш» -------- */
        PullRefreshIndicator(
            refreshing = state.isLoading,
            state      = pullState,
            modifier   = Modifier
                .align(Alignment.TopCenter)
        )
    }
}

/* -------------------------------------------------------------------------- */
/*                    SMALL RE-USABLE COMPOSABLE HELPERS                      */
/* -------------------------------------------------------------------------- */

@Composable
private fun SectionTitle(text: String) = Text(
    text   = text,
    style  = MaterialTheme.typography.titleMedium,
    color  = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(start = 16.dp)
)

@Composable
private fun FooterLoader() = Box(
    modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
    contentAlignment = Alignment.Center
) {
    CircularProgressIndicator()
}

/* ---- Заглушки (если ещё не созданы) ---- */

@Composable
fun LoadingPlaceholder() = Box(
    modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center)
) { CircularProgressIndicator() }

@Composable
fun ErrorPlaceholder(message: String, onRetry: () -> Unit) = Box(
    modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center)
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRetry) { Text("Повторить") }
    }
}