package com.example.toiletmap.data.supabase

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClientProvider{

    val client = createSupabaseClient(

        supabaseUrl = "https://abbzqdozmrgwmlddutrx.supabase.co",

        supabaseKey = "sb_publishable_gnRgh-Azq6bD-czRl6Pjqg_dX-FIoEU"

    ) {

        // メールアドレス・パスワードでの
        // 登録、ログイン、ログアウト
        install(Auth)

        // PostgreSQLデータベースとの通信
        install(Postgrest)

        // プロフィール写真などのファイル保存
        install(Storage)
    }
}