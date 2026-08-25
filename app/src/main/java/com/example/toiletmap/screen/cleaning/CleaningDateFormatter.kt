package com.example.toiletmap.screen.cleaning

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


private val cleaningDateFormatter =
    DateTimeFormatter
        .ofPattern(
            "yyyy/MM/dd HH:mm"
        )
        .withZone(
            ZoneId.systemDefault()
        )


fun formatCleaningDateTime(
    value: String?
): String {

    if (value.isNullOrBlank()) {
        return "未設定"
    }

    val instant =
        runCatching {
            Instant.parse(
                value
            )
        }.getOrElse {

            runCatching {
                OffsetDateTime
                    .parse(
                        value
                    )
                    .toInstant()
            }.getOrNull()
                ?: return value
        }

    return cleaningDateFormatter
        .format(
            instant
        )
}
