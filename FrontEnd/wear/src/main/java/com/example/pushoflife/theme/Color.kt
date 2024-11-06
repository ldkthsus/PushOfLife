package com.example.pushoflife.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal = Color(0xFF135427)
val Red = Color(0xFF690E0E)

internal val wearColorPalette: Colors = Colors(
    primary = Purple500,
    primaryVariant = Purple700,
    secondary = Teal,
    secondaryVariant = Teal,
    error = Red,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onError = Color.Black
)