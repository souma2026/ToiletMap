package com.example.toiletmap.data.model

data class Toilet(
    // Supabase導入後にIDを入れる
    val id: String? = null,

    // トイレ名
    val name: String,

    // 場所
    val location: String,

    // 利用可能時間
    val openingHours: String?,

    // きれいさ 1〜5
    val cleanliness: Int,

    // コメント
    val comment: String?
)