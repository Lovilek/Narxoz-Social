package com.narxoz.social.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.narxoz.social.repository.SessionManager
import com.narxoz.social.ui.comments.CommentsScreen
import com.narxoz.social.ui.navigation.LocalNavController
import com.narxoz.social.ui.likes.LikesScreen
import com.narxoz.social.ui.orgs.OrganizationsScreen
import com.narxoz.social.ui.PrivacyPolicyScreen
import com.narxoz.social.ui.notifications.NotificationsScreen
import com.narxoz.social.ui.friends.AddFriendScreen
import com.narxoz.social.ui.friends.FriendsListScreen
import com.narxoz.social.ui.profile.ProfileScreen
import com.narxoz.social.ui.profile.EditProfileScreen
import com.narxoz.social.ui.profile.AnotherProfileScreen
import com.narxoz.social.ui.post.CreatePostScreen
import com.narxoz.social.ui.post.PostDetailScreen
import com.narxoz.social.ui.post.EditPostScreen
import com.narxoz.social.ui.myposts.MyPostsScreen

@Composable
fun AppNavigation(onToggleTheme: () -> Unit) {
    val navController = rememberNavController()

    val loggedOut by SessionManager.isLoggedOut
        .collectAsState(initial = false)       // заодно задаём initial

    LaunchedEffect(loggedOut) {
        if (loggedOut) navController.navigate("login") {
            popUpTo(0)                         // очистить стек
        }
    }

    CompositionLocalProvider(LocalNavController provides navController) {

        NavHost(navController, startDestination = "login") {
            composable("login") { LoginScreen(navController) }
            composable(
                route = "policy/{role}",
                arguments = listOf(navArgument("role") { type = NavType.StringType })
            ) { backStack ->
                val role = backStack.arguments!!.getString("role") ?: "student"
                PrivacyPolicyScreen(navController, role)
            }
            composable("mainFeed") { MainFeedScreen(onToggleTheme = onToggleTheme) }
            composable("newPost") {
                CreatePostScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = "post/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { back ->
                val id = back.arguments!!.getInt("id")
                PostDetailScreen(
                    postId = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate("editPost/$id") }
                )
            }
            composable(
                route = "editPost/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { back ->
                val id = back.arguments!!.getInt("id")
                EditPostScreen(
                    postId = id,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                "comments/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.IntType })
            ) { backStack ->
                val id = backStack.arguments!!.getInt("postId")
                CommentsScreen(
                    postId = id,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                "likes/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.IntType })
            ) { backStack ->
                val id = backStack.arguments!!.getInt("postId")
                LikesScreen(
                    postId = id,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("notifications") {
                NotificationsScreen(onBack = { navController.popBackStack() })
            }
            composable("student") { MainFeedScreen(onToggleTheme = onToggleTheme) }
            composable("teacher") { MainFeedScreen(onToggleTheme = onToggleTheme) }
            composable("organizations") { OrganizationsScreen() }
            composable("addFriend") {
                AddFriendScreen(onBack = { navController.popBackStack() })
            }
            composable("friends") {
                FriendsListScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = "user/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStack ->
                val id = backStack.arguments!!.getInt("id")
                AnotherProfileScreen(
                    userId = id,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("profile") {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate("editProfile") },
                    onMyPosts = { navController.navigate("myPosts") }
                )
            }
            composable("myPosts") {
                MyPostsScreen(onBack = { navController.popBackStack() })
            }
            composable("editProfile") {
                EditProfileScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}