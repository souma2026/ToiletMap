package com.example.toiletmap.screen.cleaning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toiletmap.model.CleaningRequest
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.model.Toilet


private val CleaningGreen =
    Color(0xFF0B8377)

private val CleaningDark =
    Color(0xFF12313A)

private val CleaningMuted =
    Color(0xFF748186)

private val CleaningPale =
    Color(0xFFF5F8F7)

private val CleaningBlue =
    Color(0xFF1976D2)

private val CleaningRed =
    Color(0xFFD94B4B)


@Composable
fun CleaningScreen(
    requests: List<CleaningRequest>,
    toilets: List<Toilet>,
    currentUserId: String?,
    isLoading: Boolean,
    actionRequestId: String?,
    onRefresh: () -> Unit,
    onShowOnMap: (CleaningRequest) -> Unit,
    onCancelCleaning: (CleaningRequest) -> Unit,
    onOpenUncleaned: () -> Unit,
    onOpenAccount: () -> Unit
) {

    val myAssignments =
        requests
            .filter {
                it.status == CleaningStatus.IN_PROGRESS &&
                        it.cleanerId == currentUserId
            }

    val toiletsById =
        toilets.associateBy {
            it.id
        }


    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    CleaningPale
                )
                .padding(
                    horizontal = 16.dp,
                    vertical = 14.dp
                )
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                Text(
                    text =
                        "CLEANING",
                    color =
                        CleaningGreen,
                    fontSize =
                        10.sp,
                    fontWeight =
                        FontWeight.ExtraBold,
                    letterSpacing =
                        1.5.sp
                )

                Text(
                    text =
                        "清掃",
                    color =
                        CleaningDark,
                    style =
                        MaterialTheme.typography.headlineSmall,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            IconButton(
                onClick =
                    onRefresh
            ) {

                Icon(
                    imageVector =
                        Icons.Outlined.Refresh,
                    contentDescription =
                        "更新",
                    tint =
                        CleaningGreen
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(
                    14.dp
                )
        )


        Text(
            text =
                "現在担当している清掃",
            color =
                CleaningDark,
            style =
                MaterialTheme.typography.titleMedium,
            fontWeight =
                FontWeight.Bold
        )


        Spacer(
            modifier =
                Modifier.height(
                    10.dp
                )
        )


        when {

            isLoading &&
                    currentUserId == null -> {

                Box(
                    modifier =
                        Modifier.fillMaxSize(),
                    contentAlignment =
                        Alignment.Center
                ) {

                    CircularProgressIndicator(
                        color =
                            CleaningGreen
                    )
                }
            }

            currentUserId == null -> {

                CleaningEmptyState(
                    icon =
                        Icons.Outlined.Person,
                    title =
                        "ログインが必要です",
                    message =
                        "清掃を引き受けたり、担当中の清掃を確認したりするにはログインしてください。",
                    buttonText =
                        "アカウントを開く",
                    onClick =
                        onOpenAccount
                )
            }

            isLoading && myAssignments.isEmpty() -> {

                Box(
                    modifier =
                        Modifier.fillMaxSize(),
                    contentAlignment =
                        Alignment.Center
                ) {

                    CircularProgressIndicator(
                        color =
                            CleaningGreen
                    )
                }
            }

            myAssignments.isEmpty() -> {

                CleaningEmptyState(
                    icon =
                        Icons.Outlined.CleaningServices,
                    title =
                        "現在担当している清掃はありません",
                    message =
                        "未清掃一覧から、清掃したいトイレを探してみましょう。",
                    buttonText =
                        "未清掃一覧を見る",
                    onClick =
                        onOpenUncleaned
                )
            }

            else -> {

                LazyColumn(
                    modifier =
                        Modifier.fillMaxSize(),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            12.dp
                        )
                ) {

                    items(
                        items =
                            myAssignments,
                        key = {
                            it.id
                        }
                    ) {
                            request ->

                        CleaningAssignmentCard(
                            request =
                                request,
                            toilet =
                                toiletsById[
                                    request.toiletId
                                ],
                            isActionInProgress =
                                actionRequestId == request.id,
                            onShowOnMap = {
                                onShowOnMap(
                                    request
                                )
                            },
                            onCancelCleaning = {
                                onCancelCleaning(
                                    request
                                )
                            }
                        )
                    }

                    item {

                        Spacer(
                            modifier =
                                Modifier.height(
                                    16.dp
                                )
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun CleaningAssignmentCard(
    request: CleaningRequest,
    toilet: Toilet?,
    isActionInProgress: Boolean,
    onShowOnMap: () -> Unit,
    onCancelCleaning: () -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                20.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    3.dp
            )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        16.dp
                    ),
            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Surface(
                    color =
                        CleaningBlue.copy(
                            alpha = 0.12f
                        ),
                    shape =
                        RoundedCornerShape(
                            10.dp
                        )
                ) {

                    Icon(
                        imageVector =
                            Icons.Outlined.CleaningServices,
                        contentDescription =
                            null,
                        tint =
                            CleaningBlue,
                        modifier =
                            Modifier.padding(
                                10.dp
                            )
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(
                            12.dp
                        )
                )

                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {

                    Text(
                        text =
                            toilet?.name
                                ?: "トイレ情報を取得中",
                        color =
                            CleaningDark,
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight =
                            FontWeight.Bold
                    )

                    if (toilet != null) {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Outlined.LocationOn,
                                contentDescription =
                                    null,
                                tint =
                                    CleaningMuted,
                                modifier =
                                    Modifier.size(
                                        15.dp
                                    )
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(
                                        3.dp
                                    )
                            )

                            Text(
                                text =
                                    "緯度 %.4f / 経度 %.4f".format(
                                        toilet.latitude,
                                        toilet.longitude
                                    ),
                                color =
                                    CleaningMuted,
                                fontSize =
                                    11.sp
                            )
                        }
                    }
                }

                Surface(
                    color =
                        CleaningBlue.copy(
                            alpha = 0.12f
                        ),
                    shape =
                        RoundedCornerShape(
                            8.dp
                        )
                ) {

                    Text(
                        text =
                            "清掃中",
                        modifier =
                            Modifier.padding(
                                horizontal = 9.dp,
                                vertical = 5.dp
                            ),
                        color =
                            CleaningBlue,
                        fontSize =
                            12.sp,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }


            CleaningInfoRow(
                label =
                    "依頼日時",
                value =
                    formatCleaningDateTime(
                        request.requestedAt
                    )
            )

            CleaningInfoRow(
                label =
                    "引受日時",
                value =
                    formatCleaningDateTime(
                        request.acceptedAt
                    )
            )

            CleaningInfoRow(
                label =
                    "予定報酬",
                value =
                    "${request.rewardPoints} pt"
            )


            Button(
                onClick =
                    onShowOnMap,
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(
                        12.dp
                    ),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            CleaningGreen
                    )
            ) {

                Icon(
                    imageVector =
                        Icons.Outlined.Map,
                    contentDescription =
                        null
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )

                Text(
                    text =
                        "地図で確認する"
                )
            }


            OutlinedButton(
                onClick = {},
                modifier =
                    Modifier.fillMaxWidth(),
                enabled =
                    false,
                shape =
                    RoundedCornerShape(
                        12.dp
                    )
            ) {

                Icon(
                    imageVector =
                        Icons.Outlined.CheckCircle,
                    contentDescription =
                        null
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )

                Text(
                    text =
                        "清掃完了（次の段階で実装）"
                )
            }


            OutlinedButton(
                onClick =
                    onCancelCleaning,
                modifier =
                    Modifier.fillMaxWidth(),
                enabled =
                    !isActionInProgress,
                shape =
                    RoundedCornerShape(
                        12.dp
                    ),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor =
                            CleaningRed
                    )
            ) {

                if (isActionInProgress) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(
                                18.dp
                            ),
                        strokeWidth =
                            2.dp
                    )

                    Spacer(
                        modifier =
                            Modifier.width(
                                8.dp
                            )
                    )
                }

                Text(
                    text =
                        "清掃担当をキャンセル"
                )
            }


            Text(
                text =
                    "清掃完了と報酬ポイント付与は、次の実装段階で有効になります。",
                color =
                    CleaningMuted,
                fontSize =
                    11.sp
            )
        }
    }
}


@Composable
private fun CleaningInfoRow(
    label: String,
    value: String
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text =
                label,
            color =
                CleaningMuted,
            fontSize =
                13.sp
        )

        Text(
            text =
                value,
            color =
                CleaningDark,
            fontSize =
                13.sp,
            fontWeight =
                FontWeight.SemiBold
        )
    }
}


@Composable
private fun CleaningEmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    buttonText: String,
    onClick: () -> Unit
) {

    Box(
        modifier =
            Modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center
    ) {

        Card(
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                RoundedCornerShape(
                    22.dp
                ),
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        Color.White
                )
        ) {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            24.dp
                        ),
                horizontalAlignment =
                    Alignment.CenterHorizontally,
                verticalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {

                Surface(
                    color =
                        CleaningGreen.copy(
                            alpha = 0.1f
                        ),
                    shape =
                        RoundedCornerShape(
                            18.dp
                        )
                ) {

                    Icon(
                        imageVector =
                            icon,
                        contentDescription =
                            null,
                        tint =
                            CleaningGreen,
                        modifier =
                            Modifier
                                .padding(
                                    16.dp
                                )
                                .size(
                                    36.dp
                                )
                    )
                }

                Text(
                    text =
                        title,
                    color =
                        CleaningDark,
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight =
                        FontWeight.Bold,
                    textAlign =
                        TextAlign.Center
                )

                Text(
                    text =
                        message,
                    color =
                        CleaningMuted,
                    style =
                        MaterialTheme.typography.bodyMedium,
                    textAlign =
                        TextAlign.Center
                )

                Button(
                    onClick =
                        onClick,
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(
                            12.dp
                        ),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                CleaningGreen
                        )
                ) {

                    Text(
                        text =
                            buttonText
                    )
                }
            }
        }
    }
}
