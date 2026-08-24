package com.example.toiletmap.screen.listofuncleaned


/*
 * =====================================
 * 未清掃一覧画面専用のトイレ情報
 * =====================================
 */
data class UncleanedToilet(

    val id: String,

    val name: String,

    val latitude: Double,

    val longitude: Double,

    val lastCleanedAtMillis: Long? = null
)