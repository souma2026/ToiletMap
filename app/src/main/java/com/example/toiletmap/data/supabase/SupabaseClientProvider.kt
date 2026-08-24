package com.example.toiletmap.data.supabase

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage


object SupabaseClientProvider {

    val client =
        createSupabaseClient(

            supabaseUrl =
                "https://abbzqdozmrgwmlddutrx.supabase.co",

            supabaseKey =
                "sb_publishable_gnRgh-Azq6bD-czRl6Pjqg_dX-FIoEU"

        ) {

            // =========================================
            // Auth
            // =========================================
            //
            // ログイン・新規登録・ログアウト
            //
            // autoLoadFromStorage
            // → アプリ起動時に保存済みセッションを読み込む
            //
            // autoSaveToStorage
            // → ログイン状態が変わったら端末へ保存する
            //
            // alwaysAutoRefresh
            // → セッション期限が近づいたら自動更新する
            //
            // =========================================

            install(Auth) {

                autoLoadFromStorage =
                    true

                autoSaveToStorage =
                    true

                alwaysAutoRefresh =
                    true
            }


            // =========================================
            // PostgreSQL
            // =========================================

            install(Postgrest)


            // =========================================
            // Storage
            // =========================================
            //
            // プロフィール写真などを保存
            // =========================================

            install(Storage)
        }
}