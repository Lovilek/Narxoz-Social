package com.narxoz.social.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val LightColorScheme = lightColorScheme(
    primary       = Color(0xFFD6001C),
    onPrimary     = Color.White,
    secondary     = Color(0xFFFFC0CB),
    background    = Color(0xFFFFFFFF),
    surface       = Color(0xFFFDFDFD),
    onSurface     = Color(0xFF202020),
)

val DarkColorScheme = darkColorScheme(
    primary       = Color(0xFFFF576B),
    onPrimary     = Color.Black,
    secondary     = Color(0xFF8B4350),
    background    = Color(0xFF121212),
    surface       = Color(0xFF1E1E1E),
    onSurface     = Color(0xFFEDEDED),
)