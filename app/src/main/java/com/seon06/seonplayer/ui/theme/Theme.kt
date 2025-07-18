package com.seon06.seonplayer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.seon06.seonplayer.R

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun SeonPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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

    val paperFontFamily = FontFamily(
        Font(R.font.paperlogy3, FontWeight.Light),
        Font(R.font.paperlogy6, FontWeight.Normal),
        Font(R.font.paperlogy8, FontWeight.Bold),
        Font(R.font.paperlogy9, FontWeight.ExtraBold),
    )

    val myTypography = Typography(
        bodySmall =  TextStyle(
            fontFamily = paperFontFamily, fontWeight = FontWeight.Light, fontSize = 12.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = paperFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = paperFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp
        )
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = myTypography,
        content = content,
    )
}