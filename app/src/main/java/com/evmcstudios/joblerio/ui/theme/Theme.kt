package com.evmcstudios.joblerio.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBBBCFF),
    onPrimary = Color(0xFF161B57),
    primaryContainer = Color(0xFF2E3494),
    onPrimaryContainer = Color(0xFFDFE0FF),
    secondary = Color(0xFF5D5E72),
    onSecondary = Color(0xFF2C2F42),
    secondaryContainer = Color(0xFF434459),
    onSecondaryContainer = Color(0xFFD9DAF0),
    tertiary = Color(0xFF7A5465),
    background = Color(0xFF121318),
    onBackground = Color(0xFFE3E1E6),
    surface = Color(0xFF121318),
    onSurface = Color(0xFFE3E1E6),
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0DEFF),
    onPrimaryContainer = Color(0xFF0F0086),
    secondary = Color(0xFF5D5E72),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9DAF0),
    onSecondaryContainer = Color(0xFF1A1B2E),
    tertiary = Color(0xFF7A5465),
    background = BackgroundWhite,
    onBackground = TitleDark,
    surface = CardWhite,
    onSurface = TitleDark,
)

@Composable
fun JoblerioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
