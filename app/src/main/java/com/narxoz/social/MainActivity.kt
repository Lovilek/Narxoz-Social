package com.narxoz.social

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.narxoz.social.ui.theme.NarxozTheme
import com.narxoz.social.ui.AppNavigation
import com.narxoz.social.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: ThemeViewModel = viewModel()
            val dark by vm.isDark.collectAsState()

            NarxozTheme(darkTheme = dark) {            // ← передаём выбор
                AppNavigation(
                    onToggleTheme = vm::toggle        // прокидываем в UI
                )
            }
        }
    }
}