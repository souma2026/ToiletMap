package com.example.toiletmap.data

import com.example.toiletmap.data.model.Toilet

data class ToiletValidationResult(
    val toiletNameError: Boolean,
    val locationError: Boolean
) {
    val isValid: Boolean
        get() = !toiletNameError && !locationError
}

object ToiletDataProcessor {

    // 入力内容のチェック
    fun validate(
        toiletName: String,
        location: String
    ): ToiletValidationResult {

        return ToiletValidationResult(
            toiletNameError = toiletName.isBlank(),
            locationError = location.isBlank()
        )
    }

    // 入力された文字列からToiletデータを作成
    fun createToilet(
        toiletName: String,
        location: String,
        openingHours: String,
        cleanliness: Float,
        comment: String
    ): Toilet {

        return Toilet(
            name = toiletName.trim(),

            location = location.trim(),

            openingHours = openingHours
                .trim()
                .ifBlank { null },

            cleanliness = cleanliness
                .toInt()
                .coerceIn(1, 5),

            comment = comment
                .trim()
                .ifBlank { null }
        )
    }
}