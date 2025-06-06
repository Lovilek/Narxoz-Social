package com.narxoz.social.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val NarxozTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = NarxozFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NarxozFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NarxozFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp
    )
)