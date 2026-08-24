package com.example.toiletmap.model

import java.util.UUID

enum class CleaningStatus {

    // 通常状態：赤いピン
    NORMAL,

    // 清掃依頼中：黄色いピン
    REQUESTED
}

data class Toilet(

    // 各トイレを区別するID
    val id: String = UUID.randomUUID().toString(),

    // トイレ名
    val name: String,

    // 緯度
    val latitude: Double,

    // 経度
    val longitude: Double,

    // 清潔度 1～5
    val cleanliness: Int,

    // コメント
    val comment: String,

    // 清掃状態
    val cleaningStatus: CleaningStatus = CleaningStatus.NORMAL,

    // 前回「清掃しました」を押した時刻
    // まだ清掃した記録がない場合は null
    val lastCleanedAtMillis: Long? = null
)