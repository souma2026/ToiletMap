package com.example.toiletmap.screen.review

import com.example.toiletmap.model.ToiletReview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt


private val ReviewGreen =
    Color(0xFF0B8377)

private val ReviewDark =
    Color(0xFF12313A)

private val ReviewMuted =
    Color(0xFF748186)

private val ReviewPale =
    Color(0xFFF5F8F7)

private val ReviewAmber =
    Color(0xFFF2B544)

private val ReviewError =
    Color(0xFFB3261E)


@Composable
fun ReviewOpenButton(

    reviewCount: Int,

    onClick: () -> Unit,

    modifier: Modifier =
        Modifier
) {

    Button(

        onClick =
            onClick,

        modifier =
            modifier
                .shadow(
                    elevation =
                        7.dp,

                    shape =
                        RoundedCornerShape(
                            50
                        )
                ),

        shape =
            RoundedCornerShape(
                50
            ),

        colors =
            ButtonDefaults
                .buttonColors(

                    containerColor =
                        Color.White,

                    contentColor =
                        ReviewGreen
                ),

        contentPadding =
            PaddingValues(

                horizontal =
                    18.dp,

                vertical =
                    10.dp
            )

    ) {

        Text(

            text =
                "口コミ ${reviewCount}件",

            fontWeight =
                FontWeight.Bold
        )
    }
}


@Composable
fun ReviewDialog(

    toiletName: String,

    reviews:
    List<ToiletReview>,

    isLoading: Boolean,

    isPosting: Boolean,

    currentUserId: String?,

    errorMessage: String?,

    successMessage: String?,

    onReload: () -> Unit,

    onSubmit: (
        Int,
        String
    ) -> Unit,

    onDelete: (String) -> Unit,

    onDismiss: () -> Unit
) {

    var rating by
    rememberSaveable(
        toiletName
    ) {

        mutableIntStateOf(
            5
        )
    }


    var comment by
    rememberSaveable(
        toiletName
    ) {

        mutableStateOf(
            ""
        )
    }


    var reviewIdPendingDelete by
    rememberSaveable(
        toiletName
    ) {

        mutableStateOf<String?>(
            null
        )
    }


    LaunchedEffect(
        successMessage
    ) {

        if (
            successMessage !=
            null
        ) {

            rating =
                5

            comment =
                ""
        }
    }


    val averageRating =
        if (
            reviews.isEmpty()
        ) {

            null

        } else {

            reviews
                .map {
                        review ->

                    review.rating
                }
                .average()
        }


    /*
     * 自分が投稿した口コミ
     */
    val myReview =
        currentUserId
            ?.let {
                    userId ->

                reviews
                    .firstOrNull {
                            review ->

                        review.userId ==
                                userId
                    }
            }


    /*
     * =====================================
     * 削除確認
     * =====================================
     */
    if (
        reviewIdPendingDelete !=
        null
    ) {

        AlertDialog(

            onDismissRequest = {

                if (
                    !isPosting
                ) {

                    reviewIdPendingDelete =
                        null
                }
            },

            title = {

                Text(
                    text =
                        "口コミを削除"
                )
            },

            text = {

                Text(
                    text =
                        "この口コミを削除しますか？削除後は、このトイレへ新しい口コミを1件投稿できます。"
                )
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        val reviewId =
                            reviewIdPendingDelete
                                ?: return@TextButton


                        onDelete(
                            reviewId
                        )


                        reviewIdPendingDelete =
                            null
                    },

                    enabled =
                        !isPosting

                ) {

                    Text(

                        text =
                            "削除",

                        color =
                            ReviewError
                    )
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {

                        reviewIdPendingDelete =
                            null
                    },

                    enabled =
                        !isPosting

                ) {

                    Text(
                        text =
                            "キャンセル"
                    )
                }
            }
        )
    }


    Dialog(

        onDismissRequest =
            onDismiss,

        properties =
            DialogProperties(

                usePlatformDefaultWidth =
                    false
            )

    ) {

        Surface(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(
                        0.94f
                    )
                    .padding(
                        12.dp
                    )
                    .imePadding(),

            shape =
                RoundedCornerShape(
                    26.dp
                ),

            color =
                Color.White,

            shadowElevation =
                12.dp

        ) {

            Column(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            18.dp
                        )
            ) {

                /*
                 * =====================================
                 * タイトル
                 * =====================================
                 */
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
                                "口コミ",

                            color =
                                ReviewGreen,

                            fontSize =
                                13.sp,

                            fontWeight =
                                FontWeight.ExtraBold,

                            letterSpacing =
                                1.2.sp
                        )


                        Text(

                            text =
                                toiletName,

                            color =
                                ReviewDark,

                            style =
                                MaterialTheme
                                    .typography
                                    .titleLarge,

                            fontWeight =
                                FontWeight.Bold,

                            maxLines =
                                1,

                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }


                    TextButton(

                        onClick =
                            onDismiss

                    ) {

                        Text(

                            text =
                                "閉じる",

                            color =
                                ReviewGreen
                        )
                    }
                }


                Spacer(

                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )


                /*
                 * =====================================
                 * 平均評価
                 * =====================================
                 */
                Card(

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(
                            18.dp
                        ),

                    colors =
                        CardDefaults
                            .cardColors(

                                containerColor =
                                    ReviewPale
                            )

                ) {

                    Row(

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(

                                    horizontal =
                                        16.dp,

                                    vertical =
                                        13.dp
                                ),

                        horizontalArrangement =
                            Arrangement.SpaceBetween,

                        verticalAlignment =
                            Alignment.CenterVertically

                    ) {

                        Column {

                            Text(

                                text =
                                    "みんなの評価",

                                color =
                                    ReviewMuted,

                                fontSize =
                                    12.sp
                            )


                            Text(

                                text =
                                    if (
                                        averageRating ==
                                        null
                                    ) {

                                        "まだ評価はありません"

                                    } else {

                                        "${formatAverage(averageRating)} / 5.0"
                                    },

                                color =
                                    ReviewDark,

                                fontSize =
                                    20.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }


                        Column(

                            horizontalAlignment =
                                Alignment.End
                        ) {

                            Text(

                                text =
                                    if (
                                        averageRating ==
                                        null
                                    ) {

                                        "☆☆☆☆☆"

                                    } else {

                                        ratingStars(

                                            averageRating
                                                .roundToInt()
                                        )
                                    },

                                color =
                                    ReviewAmber,

                                fontSize =
                                    20.sp
                            )


                            Text(

                                text =
                                    "${reviews.size}件",

                                color =
                                    ReviewMuted,

                                fontSize =
                                    12.sp
                            )
                        }
                    }
                }


                Spacer(

                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )


                HorizontalDivider()


                /*
                 * =====================================
                 * 口コミ一覧
                 * =====================================
                 */
                Box(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(
                                1f
                            )
                ) {

                    when {

                        isLoading &&
                                reviews.isEmpty() -> {

                            Column(

                                modifier =
                                    Modifier.fillMaxSize(),

                                horizontalAlignment =
                                    Alignment.CenterHorizontally,

                                verticalArrangement =
                                    Arrangement.Center

                            ) {

                                CircularProgressIndicator(

                                    color =
                                        ReviewGreen
                                )


                                Spacer(

                                    modifier =
                                        Modifier.height(
                                            10.dp
                                        )
                                )


                                Text(

                                    text =
                                        "口コミを読み込んでいます",

                                    color =
                                        ReviewMuted
                                )
                            }
                        }


                        reviews.isEmpty() -> {

                            Column(

                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .padding(
                                            20.dp
                                        ),

                                horizontalAlignment =
                                    Alignment.CenterHorizontally,

                                verticalArrangement =
                                    Arrangement.Center

                            ) {

                                Text(

                                    text =
                                        "まだ口コミがありません",

                                    color =
                                        ReviewDark,

                                    fontWeight =
                                        FontWeight.Bold
                                )


                                Text(

                                    text =
                                        "最初の口コミを投稿してみましょう。",

                                    color =
                                        ReviewMuted,

                                    fontSize =
                                        13.sp
                                )


                                if (
                                    errorMessage !=
                                    null
                                ) {

                                    Spacer(

                                        modifier =
                                            Modifier.height(
                                                10.dp
                                            )
                                    )


                                    TextButton(

                                        onClick =
                                            onReload

                                    ) {

                                        Text(
                                            "再読み込み"
                                        )
                                    }
                                }
                            }
                        }


                        else -> {

                            LazyColumn(

                                modifier =
                                    Modifier.fillMaxSize(),

                                contentPadding =
                                    PaddingValues(

                                        vertical =
                                            12.dp
                                    ),

                                verticalArrangement =
                                    Arrangement.spacedBy(
                                        10.dp
                                    )

                            ) {

                                items(

                                    items =
                                        reviews,

                                    key = {
                                            review ->

                                        review.id
                                    }

                                ) {
                                        review ->


                                    ReviewCard(

                                        review =
                                            review,

                                        isOwnReview =
                                            currentUserId !=
                                                    null &&
                                                    review.userId ==
                                                    currentUserId,

                                        isBusy =
                                            isPosting,

                                        onDelete = {

                                            reviewIdPendingDelete =
                                                review.id
                                        }
                                    )
                                }
                            }
                        }
                    }
                }


                HorizontalDivider()


                Spacer(

                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )


                /*
                 * =====================================
                 * 投稿欄
                 * =====================================
                 */
                when {

                    currentUserId ==
                            null -> {

                        Text(

                            text =
                                "口コミを投稿するにはログインが必要です",

                            color =
                                ReviewMuted,

                            fontSize =
                                13.sp,

                            modifier =
                                Modifier.padding(

                                    vertical =
                                        12.dp
                                )
                        )
                    }


                    /*
                     * 自分のレビューが存在する場合は
                     * 投稿欄を表示しない。
                     */
                    myReview !=
                            null -> {

                        Card(

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(

                                        vertical =
                                            10.dp
                                    ),

                            shape =
                                RoundedCornerShape(
                                    16.dp
                                ),

                            colors =
                                CardDefaults
                                    .cardColors(

                                        containerColor =
                                            ReviewPale
                                    )

                        ) {

                            Column(

                                modifier =
                                    Modifier.padding(
                                        14.dp
                                    )
                            ) {

                                Text(

                                    text =
                                        "このトイレには口コミを投稿済みです",

                                    color =
                                        ReviewDark,

                                    fontWeight =
                                        FontWeight.Bold
                                )


                                Spacer(

                                    modifier =
                                        Modifier.height(
                                            4.dp
                                        )
                                )


                                Text(

                                    text =
                                        "投稿し直す場合は、上に表示されている自分の口コミを削除してください。",

                                    color =
                                        ReviewMuted,

                                    fontSize =
                                        12.sp
                                )
                            }
                        }
                    }


                    /*
                     * 未投稿なら投稿欄
                     */
                    else -> {

                        Text(

                            text =
                                "口コミを投稿",

                            color =
                                ReviewDark,

                            fontWeight =
                                FontWeight.Bold
                        )


                        Row(

                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.Center,

                            verticalAlignment =
                                Alignment.CenterVertically

                        ) {

                            for (
                            star in
                            1..5
                            ) {

                                TextButton(

                                    onClick = {

                                        rating =
                                            star
                                    },

                                    enabled =
                                        !isPosting,

                                    contentPadding =
                                        PaddingValues(
                                            1.dp
                                        )

                                ) {

                                    Text(

                                        text =
                                            if (
                                                star <=
                                                rating
                                            ) {

                                                "★"

                                            } else {

                                                "☆"
                                            },

                                        color =
                                            ReviewAmber,

                                        fontSize =
                                            30.sp
                                    )
                                }
                            }
                        }


                        OutlinedTextField(

                            value =
                                comment,

                            onValueChange = {
                                    value ->

                                if (
                                    value.length <=
                                    500
                                ) {

                                    comment =
                                        value
                                }
                            },

                            modifier =
                                Modifier.fillMaxWidth(),

                            enabled =
                                !isPosting,

                            label = {

                                Text(
                                    "口コミ本文"
                                )
                            },

                            placeholder = {

                                Text(
                                    "例：きれいで使いやすかったです"
                                )
                            },

                            minLines =
                                2,

                            maxLines =
                                4,

                            supportingText = {

                                Text(
                                    "${comment.length} / 500文字"
                                )
                            }
                        )
                    }
                }


                if (
                    errorMessage !=
                    null
                ) {

                    Text(

                        text =
                            errorMessage,

                        color =
                            ReviewError,

                        fontSize =
                            13.sp,

                        modifier =
                            Modifier.padding(

                                top =
                                    4.dp
                            )
                    )
                }


                if (
                    successMessage !=
                    null
                ) {

                    Text(

                        text =
                            successMessage,

                        color =
                            ReviewGreen,

                        fontSize =
                            13.sp,

                        fontWeight =
                            FontWeight.Bold,

                        modifier =
                            Modifier.padding(

                                top =
                                    4.dp
                            )
                    )
                }


                /*
                 * 自分の口コミが無いときだけ
                 * 投稿ボタンを表示。
                 */
                if (
                    currentUserId !=
                    null &&
                    myReview ==
                    null
                ) {

                    Spacer(

                        modifier =
                            Modifier.height(
                                8.dp
                            )
                    )


                    Button(

                        onClick = {

                            onSubmit(
                                rating,
                                comment
                            )
                        },

                        enabled =
                            comment
                                .isNotBlank() &&
                                    !isPosting,

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    50.dp
                                ),

                        shape =
                            RoundedCornerShape(
                                16.dp
                            ),

                        colors =
                            ButtonDefaults
                                .buttonColors(

                                    containerColor =
                                        ReviewGreen,

                                    contentColor =
                                        Color.White
                                )

                    ) {

                        if (
                            isPosting
                        ) {

                            CircularProgressIndicator(

                                modifier =
                                    Modifier.size(
                                        22.dp
                                    ),

                                color =
                                    Color.White,

                                strokeWidth =
                                    2.dp
                            )

                        } else {

                            Text(

                                text =
                                    "投稿する",

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ReviewCard(

    review: ToiletReview,

    isOwnReview: Boolean,

    isBusy: Boolean,

    onDelete: () -> Unit
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                17.dp
            ),

        colors =
            CardDefaults
                .cardColors(

                    containerColor =
                        ReviewPale
                )

    ) {

        Column(

            modifier =
                Modifier.padding(
                    14.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    6.dp
                )
        ) {

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically

            ) {

                Text(

                    text =
                        review.username,

                    color =
                        ReviewDark,

                    fontWeight =
                        FontWeight.Bold,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis,

                    modifier =
                        Modifier.weight(
                            1f
                        )
                )


                Text(

                    text =
                        formatReviewDate(
                            review.createdAt
                        ),

                    color =
                        ReviewMuted,

                    fontSize =
                        11.sp
                )
            }


            Text(

                text =
                    ratingStars(
                        review.rating
                    ),

                color =
                    ReviewAmber,

                fontSize =
                    18.sp
            )


            Text(

                text =
                    review.comment,

                color =
                    ReviewDark,

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )


            /*
             * 自分のレビューだけ削除ボタン表示
             */
            if (
                isOwnReview
            ) {

                Row(

                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.End

                ) {

                    TextButton(

                        onClick =
                            onDelete,

                        enabled =
                            !isBusy

                    ) {

                        Text(

                            text =
                                if (
                                    isBusy
                                ) {

                                    "処理中"

                                } else {

                                    "自分の口コミを削除"
                                },

                            color =
                                ReviewError
                        )
                    }
                }
            }
        }
    }
}


private fun ratingStars(
    rating: Int
): String {

    val safeRating =
        rating.coerceIn(
            0,
            5
        )


    return "★".repeat(
        safeRating
    ) +
            "☆".repeat(
                5 -
                        safeRating
            )
}


private fun formatAverage(
    average: Double
): String {

    return String.format(

        Locale.JAPAN,

        "%.1f",

        average
    )
}


private fun formatReviewDate(
    createdAt: String
): String {

    return try {

        OffsetDateTime
            .parse(
                createdAt
            )
            .atZoneSameInstant(
                ZoneId.systemDefault()
            )
            .format(

                DateTimeFormatter.ofPattern(

                    "yyyy/MM/dd HH:mm",

                    Locale.JAPAN
                )
            )

    } catch (
        _: Exception
    ) {

        createdAt
            .replace(
                "T",
                " "
            )
            .take(
                16
            )
    }
}