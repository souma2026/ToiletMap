package com.example.toiletmap.screen.account

import android.util.Patterns
import java.text.Normalizer
import java.util.Locale


/*
 * =========================================
 * メールアドレス正規化
 * =========================================
 */
internal fun normalizeEmail(
    email: String
): String {

    return Normalizer
        .normalize(
            email,
            Normalizer.Form.NFKC
        )
        .trim()
        .replace("\u200B", "")
        .replace("\u200C", "")
        .replace("\u200D", "")
        .replace("\uFEFF", "")
        .lowercase(
            Locale.ROOT
        )
}


/*
 * =========================================
 * メールアドレス形式チェック
 * =========================================
 */
internal fun isValidEmail(
    email: String
): Boolean {

    return Patterns
        .EMAIL_ADDRESS
        .matcher(
            email
        )
        .matches()
}


/*
 * =========================================
 * Supabase認証エラーを日本語へ変換
 * =========================================
 */
internal fun friendlyAuthError(
    error: Exception
): String {

    val text =
        error.message
            ?.lowercase()
            ?: ""


    return when {

        "invalid login credentials" in text -> {

            "メールアドレスまたはパスワードが正しくありません"
        }


        "email not confirmed" in text -> {

            "メールアドレスの確認が完了していません"
        }


        "user already registered" in text -> {

            "このメールアドレスはすでに登録されています"
        }


        "rate limit" in text -> {

            "短時間に操作が集中しました。少し待ってから再試行してください"
        }


        else -> {

            "処理に失敗しました"
        }
    }
}