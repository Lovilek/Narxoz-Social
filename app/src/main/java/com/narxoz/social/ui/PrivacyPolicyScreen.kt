package com.narxoz.social.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.narxoz.social.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    navController: NavController,
    role: String,
    viewModel: AuthViewModel = viewModel()
) {
    val accepted by viewModel.policyAccepted.collectAsState()

    val context = LocalContext.current
    val policyText = remember {
        context.resources
            .openRawResource(R.raw.privacy_policy_ru)
            .bufferedReader()
            .use { it.readText() }
    }

    LaunchedEffect(accepted) {
        if (accepted) {
            navigateToRole(navController, role)
        }
    }

    Scaffold(topBar = { SmallTopAppBar(title = { Text(stringResource(R.string.privacy_title)) }) }) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = policyText)
            Spacer(Modifier.height(24.dp))
            Button(onClick = {
                viewModel.acceptPolicy()
            }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.accept))
            }
        }
    }
}

private fun navigateToRole(navController: NavController, role: String) {
    when (role) {
        "student" -> navController.navigate("student") { popUpTo(0) }
        "teacher" -> navController.navigate("teacher") { popUpTo(0) }
        "admin" -> navController.navigate("mainFeed") { popUpTo(0) }
        else -> navController.navigate("mainFeed") { popUpTo(0) }
    }
}
