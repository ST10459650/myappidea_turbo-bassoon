package com.example.pillarpocket.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary          = PillarGreen,
    onPrimary        = androidx.compose.ui.graphics.Color.White,
    primaryContainer = PillarGreenPale,
    secondary        = PillarAccent,
    background       = PillarSurface,
    surface          = androidx.compose.ui.graphics.Color.White,
    error            = PillarRed,
)

private val DarkColorScheme = darkColorScheme(
    primary          = PillarGreenLight,
    onPrimary        = androidx.compose.ui.graphics.Color.Black,
    primaryContainer = PillarGreen,
    secondary        = PillarAccent,
    error            = PillarRedLight,
)

@Composable
fun PillarPocketTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}