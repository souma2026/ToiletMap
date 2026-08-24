package com.example.toiletmap.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ========================================
// ダークモード用カラーテーマ
// ========================================
private val DarkColorScheme = darkColorScheme(

    // メインカラー
    primary = PrimaryLight,

    // メインカラー上の文字
    onPrimary = TextPrimary,

    // サブカラー
    secondary = Primary,

    // サブカラー上の文字
    onSecondary = TextOnPrimary,

    // 背景
    background = DarkBackground,

    // 背景上の文字
    onBackground = DarkTextPrimary,

    // Card・NavigationBarなど
    surface = DarkSurface,

    // Surface上の文字
    onSurface = DarkTextPrimary,

    // Surfaceの補助色
    surfaceVariant = DarkBorder,

    // 補助Surface上の文字
    onSurfaceVariant = DarkTextSecondary,

    // エラー
    error = ErrorRed,

    // エラー上の文字
    onError = TextOnPrimary,

    // 枠線
    outline = DarkBorder
)


// ========================================
// ライトモード用カラーテーマ
// ========================================
private val LightColorScheme = lightColorScheme(

    // メインカラー
    // ボタン・選択中アイコンなど
    primary = Primary,

    // Primaryの上に表示する文字
    onPrimary = TextOnPrimary,

    // サブカラー
    secondary = PrimaryLight,

    // Secondaryの上に表示する文字
    onSecondary = TextPrimary,

    // 補助カラー
    tertiary = PrimaryDark,

    // 補助カラー上の文字
    onTertiary = TextOnPrimary,

    // アプリ全体の背景
    background = AppBackground,

    // 背景上の文字
    onBackground = TextPrimary,

    // Card・NavigationBarなど
    surface = CardBackground,

    // Surface上の文字
    onSurface = TextPrimary,

    // TextFieldなどの薄い背景
    surfaceVariant = InputBackground,

    // 補助Surface上の文字
    onSurfaceVariant = TextSecondary,

    // エラー
    error = ErrorRed,

    // エラー上の文字
    onError = TextOnPrimary,

    // 枠線
    outline = BorderColor
)


// ========================================
// ToiletMap 全体テーマ
// ========================================
@Composable
fun ToiletMapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),

    // false にすることで
    // Android 12以降でも端末の壁紙カラーに上書きされず、
    // ToiletMap独自のカラーを使用する
    dynamicColor: Boolean = false,

    content: @Composable () -> Unit
) {

    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}