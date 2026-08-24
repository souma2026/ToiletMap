package com.example.toiletmap.model

data class Toilet(

    // トイレの名前
    val name: String,

    // 緯度
    val latitude: Double,

    // 経度
    val longitude: Double,

    // 清潔度 1〜5
    val cleanliness: Int,

    // コメント
    val comment: String
)