package com.example.toiletmap.screen.map.facilities

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toiletmap.model.Toilet


private val FacilityBackground =
    Color(0xFFF8FAF9)

private val FacilityGroupBackground =
    Color.White

private val FacilityTitleColor =
    Color(0xFF12313A)

private val FacilityLabelColor =
    Color(0xFF748186)

private val FacilityAvailableColor =
    Color(0xFF0B8377)

private val FacilityUnavailableColor =
    Color(0xFFD94B4B)


@Composable
fun ToiletFacilitySection(
    toilet: Toilet,
    modifier: Modifier = Modifier
) {

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = FacilityBackground,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(14.dp),

        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        Text(
            text = "設備情報",
            color = FacilityTitleColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )


        /*
         * =====================================
         * 男子トイレ
         * =====================================
         */
        ToiletCountGroup(
            title = "男子トイレ",
            westernCount =
                toilet.maleWesternToiletCount,
            japaneseCount =
                toilet.maleJapaneseToiletCount
        )


        /*
         * =====================================
         * 女子トイレ
         * =====================================
         */
        ToiletCountGroup(
            title = "女子トイレ",
            westernCount =
                toilet.femaleWesternToiletCount,
            japaneseCount =
                toilet.femaleJapaneseToiletCount
        )


        /*
         * =====================================
         * その他設備
         * =====================================
         */
        FacilityBooleanRow(
            label = "ベビーチェア",
            value = toilet.hasBabyChair
        )

        FacilityBooleanRow(
            label = "おむつ交換台",
            value = toilet.hasDiaperChangingTable
        )

        FacilityBooleanRow(
            label = "車いす対応個室",
            value = toilet.hasAccessibleStall
        )

        FacilityBooleanRow(
            label = "オストメイト設備",
            value = toilet.hasOstomate
        )
    }
}


@Composable
private fun ToiletCountGroup(
    title: String,
    westernCount: Int?,
    japaneseCount: Int?
) {

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    color = FacilityGroupBackground,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp),

        verticalArrangement =
            Arrangement.spacedBy(7.dp)
    ) {

        Text(
            text = title,
            color = FacilityTitleColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        FacilityCountRow(
            label = "洋式",
            count = westernCount
        )

        FacilityCountRow(
            label = "和式",
            count = japaneseCount
        )
    }
}


@Composable
private fun FacilityCountRow(
    label: String,
    count: Int?
) {

    val valueText =
        if (count == null) {
            "情報なし"
        } else {
            "${count}台"
        }


    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text = label,
            color = FacilityLabelColor,
            fontSize = 13.sp
        )

        Text(
            text = valueText,
            color = FacilityTitleColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Composable
private fun FacilityBooleanRow(
    label: String,
    value: Boolean?
) {

    val valueText =
        when (value) {
            true -> "あり"
            false -> "なし"
            null -> "情報なし"
        }


    val valueColor =
        when (value) {
            true ->
                FacilityAvailableColor

            false ->
                FacilityUnavailableColor

            null ->
                FacilityLabelColor
        }


    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text = label,
            color = FacilityLabelColor,
            fontSize = 13.sp
        )

        Text(
            text = valueText,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}