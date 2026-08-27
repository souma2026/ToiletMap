package com.example.toiletmap.screen.cleaning

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toiletmap.model.CleaningRequest
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.model.Toilet


private const val CLEANING_TAB_ASSIGNMENTS =
    0

private const val CLEANING_TAB_MY_REQUESTS =
    1


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

private val CleaningAmber =
    Color(0xFFE28A00)

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
    onCompleteCleaning: (CleaningRequest) -> Unit,
    onCancelCleaning: (CleaningRequest) -> Unit,
    onCancelCleaningRequest: (CleaningRequest) -> Unit,
    onOpenUncleaned: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenAccount: () -> Unit
) {

    var selectedTab by
    rememberSaveable {
        mutableIntStateOf(
            CLEANING_TAB_ASSIGNMENTS
        )
    }

    var requestPendingCancellation by
    remember {
        mutableStateOf<CleaningRequest?>(
            null
        )
    }

    var requestPendingCompletion by
    remember {
        mutableStateOf<CleaningRequest?>(
            null
        )
    }


    val myAssignments =
        requests
            .filter { request ->
                request.status == CleaningStatus.IN_PROGRESS &&
                        request.cleanerId == currentUserId
            }

    val myRequests =
        requests
            .filter { request ->
                request.requesterId == currentUserId &&
                        request.isActive
            }

    val toiletsById =
        toilets.associateBy {
            it.id
        }

    val sectionTitle =
        if (
            selectedTab ==
            CLEANING_TAB_ASSIGNMENTS
        ) {
            "現在担当している清掃"
        } else {
            "自分が出した清掃依頼"
        }

    val sectionDescription =
        if (
            selectedTab ==
            CLEANING_TAB_ASSIGNMENTS
        ) {
            "引き受けた清掃の場所と進行状況を確認できます。"
        } else {
            "担当者待ち、または清掃中の依頼を確認できます。"
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
                    onRefresh,
                enabled =
                    !isLoading
            ) {

                if (isLoading) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(
                                21.dp
                            ),
                        color =
                            CleaningGreen,
                        strokeWidth =
                            2.dp
                    )

                } else {

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
        }


        Spacer(
            modifier =
                Modifier.height(
                    14.dp
                )
        )


        CleaningTabSelector(
            selectedTab =
                selectedTab,
            assignmentCount =
                myAssignments.size,
            requestCount =
                myRequests.size,
            onTabSelected = {
                    tab: Int ->

                selectedTab =
                    tab
            }
        )


        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )


        Text(
            text =
                sectionTitle,
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
                    3.dp
                )
        )

        Text(
            text =
                sectionDescription,
            color =
                CleaningMuted,
            style =
                MaterialTheme.typography.bodySmall
        )


        Spacer(
            modifier =
                Modifier.height(
                    10.dp
                )
        )


        Box(
            modifier =
                Modifier
                    .weight(
                        1f
                    )
                    .fillMaxWidth()
        ) {

            when {

                isLoading &&
                        currentUserId == null -> {

                    CleaningLoadingState()
                }

                currentUserId == null -> {

                    CleaningEmptyState(
                        icon =
                            Icons.Outlined.Person,
                        title =
                            "ログインが必要です",
                        message =
                            "担当中の清掃や、自分が出した清掃依頼を確認するにはログインしてください。",
                        buttonText =
                            "アカウントを開く",
                        onClick =
                            onOpenAccount
                    )
                }

                selectedTab ==
                        CLEANING_TAB_ASSIGNMENTS -> {

                    CleaningAssignmentsContent(
                        requests =
                            myAssignments,
                        toiletsById =
                            toiletsById,
                        isLoading =
                            isLoading,
                        actionRequestId =
                            actionRequestId,
                        onShowOnMap =
                            onShowOnMap,
                        onCompleteCleaning = {
                                request ->

                            requestPendingCompletion =
                                request
                        },
                        onCancelCleaning =
                            onCancelCleaning,
                        onOpenUncleaned =
                            onOpenUncleaned
                    )
                }

                else -> {

                    CleaningOwnRequestsContent(
                        requests =
                            myRequests,
                        toiletsById =
                            toiletsById,
                        isLoading =
                            isLoading,
                        actionRequestId =
                            actionRequestId,
                        onShowOnMap =
                            onShowOnMap,
                        onRequestCancellation = {
                                request: CleaningRequest ->

                            requestPendingCancellation =
                                request
                        },
                        onOpenMap =
                            onOpenMap
                    )
                }
            }
        }
    }


    val completionTarget =
        requestPendingCompletion

    if (completionTarget != null) {

        val toiletName =
            toiletsById[
                completionTarget.toiletId
            ]
                ?.name
                ?: "このトイレ"


        AlertDialog(
            onDismissRequest = {

                if (actionRequestId == null) {

                    requestPendingCompletion =
                        null
                }
            },
            title = {

                Text(
                    text =
                        "清掃を完了しますか？",
                    fontWeight =
                        FontWeight.Bold
                )
            },
            text = {

                Text(
                    text =
                        "${toiletName}の清掃完了を記録します。完了すると清掃報酬として${completionTarget.rewardPoints}ptを獲得します。"
                )
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        requestPendingCompletion =
                            null

                        onCompleteCleaning(
                            completionTarget
                        )
                    },
                    enabled =
                        actionRequestId == null
                ) {

                    Text(
                        text =
                            "清掃完了",
                        color =
                            CleaningGreen
                    )
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {

                        requestPendingCompletion =
                            null
                    },
                    enabled =
                        actionRequestId == null
                ) {

                    Text(
                        text =
                            "戻る"
                    )
                }
            }
        )
    }


    val cancellationTarget =
        requestPendingCancellation

    if (cancellationTarget != null) {

        AlertDialog(
            onDismissRequest = {

                if (actionRequestId == null) {

                    requestPendingCancellation =
                        null
                }
            },

            title = {

                Text(
                    text =
                        "清掃依頼を取り消しますか？",
                    fontWeight =
                        FontWeight.Bold
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        requestPendingCancellation =
                            null

                        onCancelCleaningRequest(
                            cancellationTarget
                        )
                    },
                    enabled =
                        actionRequestId == null
                ) {

                    Text(
                        text =
                            "取り消す",
                        color =
                            CleaningRed
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {

                        requestPendingCancellation =
                            null
                    },
                    enabled =
                        actionRequestId == null
                ) {

                    Text(
                        text =
                            "戻る"
                    )
                }
            }
        )
    }
}


@Composable
private fun CleaningTabSelector(
    selectedTab: Int,
    assignmentCount: Int,
    requestCount: Int,
    onTabSelected: (Int) -> Unit
) {

    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        color =
            Color(0xFFE6ECEA),
        shape =
            RoundedCornerShape(
                14.dp
            )
    ) {

        Row(
            modifier =
                Modifier.padding(
                    4.dp
                )
        ) {

            CleaningTabButton(
                modifier =
                    Modifier.weight(
                        1f
                    ),
                text =
                    "担当中 (${assignmentCount})",
                selected =
                    selectedTab ==
                            CLEANING_TAB_ASSIGNMENTS,
                onClick = {
                    onTabSelected(
                        CLEANING_TAB_ASSIGNMENTS
                    )
                }
            )

            CleaningTabButton(
                modifier =
                    Modifier.weight(
                        1f
                    ),
                text =
                    "自分の依頼 (${requestCount})",
                selected =
                    selectedTab ==
                            CLEANING_TAB_MY_REQUESTS,
                onClick = {
                    onTabSelected(
                        CLEANING_TAB_MY_REQUESTS
                    )
                }
            )
        }
    }
}


@Composable
private fun CleaningTabButton(
    modifier: Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Surface(
        modifier =
            modifier.clickable(
                onClick =
                    onClick
            ),
        color =
            if (selected) {
                Color.White
            } else {
                Color.Transparent
            },
        shape =
            RoundedCornerShape(
                10.dp
            ),
        shadowElevation =
            if (selected) {
                2.dp
            } else {
                0.dp
            }
    ) {

        Text(
            text =
                text,
            modifier =
                Modifier.padding(
                    vertical = 11.dp,
                    horizontal = 8.dp
                ),
            color =
                if (selected) {
                    CleaningGreen
                } else {
                    CleaningMuted
                },
            fontSize =
                13.sp,
            fontWeight =
                if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.SemiBold
                },
            textAlign =
                TextAlign.Center
        )
    }
}


@Composable
private fun CleaningAssignmentsContent(
    requests: List<CleaningRequest>,
    toiletsById: Map<String, Toilet>,
    isLoading: Boolean,
    actionRequestId: String?,
    onShowOnMap: (CleaningRequest) -> Unit,
    onCompleteCleaning: (CleaningRequest) -> Unit,
    onCancelCleaning: (CleaningRequest) -> Unit,
    onOpenUncleaned: () -> Unit
) {

    when {

        isLoading &&
                requests.isEmpty() -> {

            CleaningLoadingState()
        }

        requests.isEmpty() -> {

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
                        requests,
                    key = {
                        it.id
                    }
                ) {
                        request: CleaningRequest ->

                    CleaningAssignmentCard(
                        request =
                            request,
                        toilet =
                            toiletsById[
                                request.toiletId
                            ],
                        isActionInProgress =
                            actionRequestId == request.id,
                        isAnyActionInProgress =
                            actionRequestId != null,
                        onShowOnMap = {
                            onShowOnMap(
                                request
                            )
                        },
                        onCompleteCleaning = {
                            onCompleteCleaning(
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


@Composable
private fun CleaningOwnRequestsContent(
    requests: List<CleaningRequest>,
    toiletsById: Map<String, Toilet>,
    isLoading: Boolean,
    actionRequestId: String?,
    onShowOnMap: (CleaningRequest) -> Unit,
    onRequestCancellation: (CleaningRequest) -> Unit,
    onOpenMap: () -> Unit
) {

    when {

        isLoading &&
                requests.isEmpty() -> {

            CleaningLoadingState()
        }

        requests.isEmpty() -> {

            CleaningEmptyState(
                icon =
                    Icons.Outlined.Map,
                title =
                    "自分が出した清掃依頼はありません",
                message =
                    "マップでトイレを選び、詳細画面から清掃を依頼できます。",
                buttonText =
                    "マップを開く",
                onClick =
                    onOpenMap
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
                        requests,
                    key = {
                        it.id
                    }
                ) {
                        request: CleaningRequest ->

                    CleaningOwnRequestCard(
                        request =
                            request,
                        toilet =
                            toiletsById[
                                request.toiletId
                            ],
                        isActionInProgress =
                            actionRequestId == request.id,
                        isAnyActionInProgress =
                            actionRequestId != null,
                        onShowOnMap = {
                            onShowOnMap(
                                request
                            )
                        },
                        onCancelRequest = {
                            onRequestCancellation(
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


@Composable
private fun CleaningAssignmentCard(
    request: CleaningRequest,
    toilet: Toilet?,
    isActionInProgress: Boolean,
    isAnyActionInProgress: Boolean,
    onShowOnMap: () -> Unit,
    onCompleteCleaning: () -> Unit,
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

            CleaningCardHeader(
                toilet =
                    toilet,
                statusText =
                    "清掃中",
                statusColor =
                    CleaningBlue
            )

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

            CleaningMapButton(
                onClick =
                    onShowOnMap
            )

            Button(
                onClick =
                    onCompleteCleaning,
                modifier =
                    Modifier.fillMaxWidth(),
                enabled =
                    !isAnyActionInProgress,
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

                if (isActionInProgress) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(
                                18.dp
                            ),
                        color =
                            Color.White,
                        strokeWidth =
                            2.dp
                    )

                } else {

                    Icon(
                        imageVector =
                            Icons.Outlined.CheckCircle,
                        contentDescription =
                            null
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )

                Text(
                    text =
                        if (isActionInProgress) {
                            "清掃完了を記録中"
                        } else {
                            "清掃完了"
                        }
                )
            }

            OutlinedButton(
                onClick =
                    onCancelCleaning,
                modifier =
                    Modifier.fillMaxWidth(),
                enabled =
                    !isAnyActionInProgress,
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
        }
    }
}


@Composable
private fun CleaningOwnRequestCard(
    request: CleaningRequest,
    toilet: Toilet?,
    isActionInProgress: Boolean,
    isAnyActionInProgress: Boolean,
    onShowOnMap: () -> Unit,
    onCancelRequest: () -> Unit
) {

    val isWaiting =
        request.status ==
                CleaningStatus.REQUESTED

    val canCancelRequest =
        isWaiting &&
                request.cleanerId == null

    val statusText =
        if (isWaiting) {
            "担当者待ち"
        } else {
            "清掃中"
        }

    val statusColor =
        if (isWaiting) {
            CleaningAmber
        } else {
            CleaningBlue
        }


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

            CleaningCardHeader(
                toilet =
                    toilet,
                statusText =
                    statusText,
                statusColor =
                    statusColor
            )

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
                    "担当者",
                value =
                    if (isWaiting) {
                        "未定"
                    } else {
                        "決定済み"
                    }
            )

            if (!isWaiting) {

                CleaningInfoRow(
                    label =
                        "引受日時",
                    value =
                        formatCleaningDateTime(
                            request.acceptedAt
                        )
                )
            }

            CleaningInfoRow(
                label =
                    "使用した依頼ポイント",
                value =
                    "${request.requestPointsUsed} pt"
            )

            CleaningInfoRow(
                label =
                    "清掃報酬",
                value =
                    "${request.rewardPoints} pt"
            )

            CleaningMapButton(
                onClick =
                    onShowOnMap
            )

            if (canCancelRequest) {

                OutlinedButton(
                    onClick =
                        onCancelRequest,
                    modifier =
                        Modifier.fillMaxWidth(),
                    enabled =
                        !isAnyActionInProgress,
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
                            if (isActionInProgress) {
                                "清掃依頼を取り消し中"
                            } else {
                                "清掃依頼を取り消す"
                            }
                    )
                }
            }
        }
    }
}


@Composable
private fun CleaningCardHeader(
    toilet: Toilet?,
    statusText: String,
    statusColor: Color
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Surface(
            color =
                statusColor.copy(
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
                    statusColor,
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

        Spacer(
            modifier =
                Modifier.width(
                    8.dp
                )
        )

        Surface(
            color =
                statusColor.copy(
                    alpha = 0.12f
                ),
            shape =
                RoundedCornerShape(
                    8.dp
                )
        ) {

            Text(
                text =
                    statusText,
                modifier =
                    Modifier.padding(
                        horizontal = 9.dp,
                        vertical = 5.dp
                    ),
                color =
                    statusColor,
                fontSize =
                    12.sp,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}


@Composable
private fun CleaningMapButton(
    onClick: () -> Unit
) {

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
private fun CleaningLoadingState() {

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


@Composable
private fun CleaningEmptyState(
    icon: ImageVector,
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
