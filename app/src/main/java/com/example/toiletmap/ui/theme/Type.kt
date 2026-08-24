package com.example.toiletmap.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ========================================
// ToiletMap 全体の文字スタイル
// ========================================
val Typography = Typography(

    // ------------------------------------
    // 大きなタイトル
    // 例：「アカウント」「トイレを追加」
    // ------------------------------------
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.3).sp
    ),

    // ------------------------------------
    // 画面タイトル
    // 例：「マップ」「アカウント」
    // ------------------------------------
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.2).sp
    ),

    // ------------------------------------
    // 少し小さめの見出し
    // ------------------------------------
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),

    // ------------------------------------
    // カードなどの大きめタイトル
    // 例：「大阪駅中央トイレ」
    // ------------------------------------
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    // ------------------------------------
    // セクションタイトル
    // 例：「基本情報」「設備・状態」
    // ------------------------------------
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),

    // ------------------------------------
    // 小さめタイトル
    // ------------------------------------
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),

    // ------------------------------------
    // 通常の本文
    // ------------------------------------
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    ),

    // ------------------------------------
    // 説明文など
    // ------------------------------------
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.2.sp
    ),

    // ------------------------------------
    // 小さい補足文
    // 例：「徒歩3分」「最終更新：今日」
    // ------------------------------------
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.3.sp
    ),

    // ------------------------------------
    // ボタンなどに使う文字
    // ------------------------------------
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp
    ),

    // ------------------------------------
    // ボトムナビゲーションなど
    // ------------------------------------
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),

    // ------------------------------------
    // 非常に小さいラベル
    // ------------------------------------
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
)