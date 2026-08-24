package com.example.toiletmap.data.repository

import com.example.toiletmap.data.supabase.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object AccountRepository {

    private val supabase =
        SupabaseClientProvider.client


    // =========================================
    // 新規登録
    // =========================================

    suspend fun signUp(
        email: String,
        password: String,
        userName: String
    ) {

        supabase.auth.signUpWith(
            Email
        ) {

            this.email = email
            this.password = password

            data = buildJsonObject {

                put(
                    "username",
                    userName
                )
            }
        }
    }


    // =========================================
    // ログイン
    // =========================================

    suspend fun signIn(
        email: String,
        password: String
    ) {

        supabase.auth.signInWith(
            Email
        ) {

            this.email = email
            this.password = password
        }
    }


    // =========================================
    // ログアウト
    // =========================================

    suspend fun signOut() {

        supabase.auth.signOut()
    }


    // =========================================
    // 現在ログインしているユーザー
    // =========================================

    fun getCurrentUser() =
        supabase.auth.currentUserOrNull()


    // =========================================
    // ログインしているか
    // =========================================

    fun isLoggedIn(): Boolean {

        return supabase.auth
            .currentUserOrNull() != null
    }
}