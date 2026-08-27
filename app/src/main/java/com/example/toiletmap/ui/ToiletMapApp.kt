package com.example.toiletmap.ui

import android.os.SystemClock
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.toiletmap.model.CleaningRequest
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.model.Toilet
import com.example.toiletmap.model.ToiletReview
import com.example.toiletmap.screen.account.AccountScreen
import com.example.toiletmap.screen.add.AddToiletScreen
import com.example.toiletmap.screen.cleaning.CleaningScreen
import com.example.toiletmap.screen.game.SecretGameScreen
import com.example.toiletmap.screen.listofuncleaned.ListOfUncleanedScreen
import com.example.toiletmap.screen.listofuncleaned.UncleanedToilet
import com.example.toiletmap.screen.map.MapScreen
import com.example.toiletmap.screen.review.ReviewDialog
import com.example.toiletmap.ui.components.BottomNavigationBar
import com.example.toiletmap.viewmodel.GameViewModel
import org.maplibre.android.maps.MapView


@Composable
fun ToiletMapApp(

    gameViewModel:
    GameViewModel,

    mapView:
    MapView,

    toilets:
    List<Toilet>,

    searchResults:
    List<Toilet>,

    isSearchingToilets:
    Boolean,

    selectedToilet:
    Toilet?,

    isAddingToilet:
    Boolean,

    addedToilet:
    Toilet?,

    uncleanedToilets:
    List<UncleanedToilet>,

    cleaningRequests:
    List<CleaningRequest>,

    currentUserId:
    String?,

    requestPoints:
    Int,

    isLoadingCleaning:
    Boolean,

    cleaningActionRequestId:
    String?,

    reviews:
    List<ToiletReview>,

    isLoadingReviews:
    Boolean,

    isPostingReview:
    Boolean,

    reviewCurrentUserId:
    String?,

    reviewErrorMessage:
    String?,

    reviewSuccessMessage:
    String?,

    onShowUncleanedToiletOnMap:
        (UncleanedToilet) -> Unit,

    onSearchQueryChanged:
        (String) -> Unit,

    onSearchToiletSelected:
        (Toilet) -> Unit,

    onDismissSelectedToilet:
        () -> Unit,

    onRequestCleaning:
        (Toilet, Int) -> Unit,

    onAcceptCleaning:
        (CleaningRequest) -> Unit,

    onCompleteCleaning:
        (CleaningRequest) -> Unit,

    onCancelCleaning:
        (CleaningRequest) -> Unit,

    onCancelCleaningRequest:
        (CleaningRequest) -> Unit,

    onReloadCleaning:
        () -> Unit,

    onShowCleaningToiletOnMap:
        (String) -> Unit,

    onLoadReviews:
        (String) -> Unit,

    onSubmitReview:
        (
        String,
        Int,
        String
    ) -> Unit,

    onDeleteReview:
        (
        String,
        String
    ) -> Unit,

    onClearReviewMessages:
        () -> Unit,

    onAddToilet:
        (Toilet) -> Unit,

    onAddSuccessHandled:
        (Toilet) -> Unit,

    onCurrentLocationRequested:
        () -> Unit
) {

    var selectedScreen by
    rememberSaveable {

        mutableIntStateOf(
            2
        )
    }


    var isSelectingLocation by
    rememberSaveable {

        mutableStateOf(
            false
        )
    }


    var showReviewDialog by
    rememberSaveable {

        mutableStateOf(
            false
        )
    }


    var showSecretGame by
    rememberSaveable {

        mutableStateOf(
            false
        )
    }


    var secretTapCount by
    rememberSaveable {

        mutableIntStateOf(
            0
        )
    }


    var lastSecretTapAtMillis by
    rememberSaveable {

        mutableStateOf(
            0L
        )
    }


    fun handleSecretLogoTap() {

        val now =
            SystemClock.elapsedRealtime()


        secretTapCount =
            if (
                lastSecretTapAtMillis == 0L ||
                now - lastSecretTapAtMillis > 2_000L
            ) {

                1

            } else {

                secretTapCount + 1
            }


        lastSecretTapAtMillis =
            now


        if (
            secretTapCount >= 5
        ) {

            secretTapCount =
                0

            lastSecretTapAtMillis =
                0L

            showReviewDialog =
                false

            isSelectingLocation =
                false

            onClearReviewMessages()

            onDismissSelectedToilet()

            showSecretGame =
                true
        }
    }


    LaunchedEffect(
        selectedScreen
    ) {

        if (
            !showSecretGame
        ) {

            secretTapCount =
                0

            lastSecretTapAtMillis =
                0L
        }
    }


    if (
        showSecretGame
    ) {

        SecretGameScreen(

            viewModel =
                gameViewModel,

            onExit = {

                showSecretGame =
                    false

                secretTapCount =
                    0

                lastSecretTapAtMillis =
                    0L

                selectedScreen =
                    2
            }
        )


        return
    }


    /*
     * Map画面を開いただけでは
     * 現在地権限を要求しない。
     */
    LaunchedEffect(
        selectedScreen
    ) {

        when (
            selectedScreen
        ) {

            0,
            1,
            2 ->

                onReloadCleaning()
        }
    }


    /*
     * =====================================
     * トイレ追加フォーム
     * =====================================
     */
    var toiletName by
    rememberSaveable {

        mutableStateOf(
            ""
        )
    }


    var cleanliness by
    rememberSaveable {

        mutableIntStateOf(
            3
        )
    }


    var comment by
    rememberSaveable {

        mutableStateOf(
            ""
        )
    }


    var selectedLatitude by
    rememberSaveable {

        mutableStateOf<Double?>(
            null
        )
    }


    var selectedLongitude by
    rememberSaveable {

        mutableStateOf<Double?>(
            null
        )
    }


    /*
     * =====================================
     * トイレ追加成功後
     * =====================================
     */
    LaunchedEffect(
        addedToilet?.id
    ) {

        val toilet =
            addedToilet
                ?: return@LaunchedEffect


        toiletName =
            ""

        cleanliness =
            3

        comment =
            ""

        selectedLatitude =
            null

        selectedLongitude =
            null

        isSelectingLocation =
            false

        selectedScreen =
            2


        onAddSuccessHandled(
            toilet
        )
    }


    /*
     * 別のトイレに移動した場合
     * 口コミ画面を閉じる。
     */
    LaunchedEffect(
        selectedToilet?.id
    ) {

        showReviewDialog =
            false

        onClearReviewMessages()
    }


    val showBottomNavigation =

        !(
                selectedScreen == 2 &&
                        selectedToilet != null
                )


    Scaffold(

        bottomBar = {

            if (
                showBottomNavigation
            ) {

                BottomNavigationBar(

                    selectedScreen =
                        selectedScreen,

                    onScreenSelected = {
                            screen ->


                        if (
                            screen != 2
                        ) {

                            isSelectingLocation =
                                false

                            showReviewDialog =
                                false

                            onClearReviewMessages()

                            onDismissSelectedToilet()
                        }


                        selectedScreen =
                            screen
                    }
                )
            }
        }

    ) {
            innerPadding ->


        Box(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    )

        ) {

            when (
                selectedScreen
            ) {

                /*
                 * 未清掃一覧
                 */
                0 -> {

                    ListOfUncleanedScreen(

                        toilets =
                            uncleanedToilets,

                        onShowOnMap = {
                                toilet ->


                            selectedScreen =
                                2


                            onShowUncleanedToiletOnMap(
                                toilet
                            )
                        }
                    )
                }


                /*
                 * 清掃
                 */
                1 -> {

                    CleaningScreen(

                        requests =
                            cleaningRequests,

                        toilets =
                            toilets,

                        currentUserId =
                            currentUserId,

                        isLoading =
                            isLoadingCleaning,

                        actionRequestId =
                            cleaningActionRequestId,

                        onRefresh =
                            onReloadCleaning,

                        onShowOnMap = {
                                request ->


                            selectedScreen =
                                2


                            onShowCleaningToiletOnMap(
                                request.toiletId
                            )
                        },

                        onCompleteCleaning =
                            onCompleteCleaning,

                        onCancelCleaning =
                            onCancelCleaning,

                        onCancelCleaningRequest =
                            onCancelCleaningRequest,

                        onOpenUncleaned = {

                            selectedScreen =
                                0
                        },

                        onOpenMap = {

                            selectedScreen =
                                2
                        },

                        onOpenAccount = {

                            selectedScreen =
                                4
                        }
                    )
                }


                /*
                 * Map
                 */
                2 -> {

                    MapScreen(

                        mapView =
                            mapView,

                        toilets =
                            toilets,

                        searchResults =
                            searchResults,

                        isSearchingToilets =
                            isSearchingToilets,

                        onSearchQueryChanged =
                            onSearchQueryChanged,

                        onSearchToiletSelected =
                            onSearchToiletSelected,

                        onSecretLogoTap = {

                            handleSecretLogoTap()
                        },

                        onCurrentLocationClick =
                            onCurrentLocationRequested,

                        isSelectingLocation =
                            isSelectingLocation,

                        selectedToilet =
                            selectedToilet,

                        cleaningRequest =
                            selectedToilet
                                ?.let {
                                        toilet ->

                                    cleaningRequests
                                        .firstOrNull {
                                                request ->

                                            request.toiletId ==
                                                    toilet.id &&
                                                    (
                                                            request.status ==
                                                                    CleaningStatus.REQUESTED ||
                                                                    request.status ==
                                                                    CleaningStatus.IN_PROGRESS
                                                            )
                                        }
                                },

                        currentUserId =
                            currentUserId,

                        currentRequestPoints =
                            requestPoints,

                        isLoadingCleaning =
                            isLoadingCleaning,

                        cleaningActionRequestId =
                            cleaningActionRequestId,

                        onDismissSelectedToilet =
                            onDismissSelectedToilet,

                        onRequestCleaning =
                            onRequestCleaning,

                        onAcceptCleaning =
                            onAcceptCleaning,

                        onOpenCleaningScreen = {

                            selectedScreen =
                                1
                        },

                        onOpenAccount = {

                            showReviewDialog =
                                false

                            onDismissSelectedToilet()

                            selectedScreen =
                                4
                        },

                        onOpenReviews = {
                                toilet ->


                            onClearReviewMessages()


                            onLoadReviews(
                                toilet.id
                            )


                            showReviewDialog =
                                true
                        },

                        onLocationSelected = {
                                latitude,
                                longitude ->


                            selectedLatitude =
                                latitude

                            selectedLongitude =
                                longitude

                            isSelectingLocation =
                                false

                            selectedScreen =
                                3
                        },

                        onCancelLocationSelection = {

                            isSelectingLocation =
                                false

                            selectedScreen =
                                3
                        }
                    )
                }


                /*
                 * トイレ追加
                 */
                3 -> {

                    AddToiletScreen(

                        toiletName =
                            toiletName,

                        cleanliness =
                            cleanliness,

                        comment =
                            comment,

                        latitude =
                            selectedLatitude,

                        longitude =
                            selectedLongitude,

                        onToiletNameChange = {

                            toiletName =
                                it
                        },

                        onCleanlinessChange = {

                            cleanliness =
                                it
                        },

                        onCommentChange = {

                            comment =
                                it
                        },

                        onSelectLocation = {

                            onDismissSelectedToilet()

                            isSelectingLocation =
                                true

                            selectedScreen =
                                2
                        },

                        onAddToilet = {

                            val latitude =
                                selectedLatitude

                            val longitude =
                                selectedLongitude


                            if (
                                !isAddingToilet &&
                                latitude != null &&
                                longitude != null
                            ) {

                                val toilet =
                                    Toilet(

                                        name =
                                            toiletName
                                                .trim(),

                                        latitude =
                                            latitude,

                                        longitude =
                                            longitude,

                                        cleanliness =
                                            cleanliness,

                                        comment =
                                            comment
                                                .trim()
                                    )


                                onAddToilet(
                                    toilet
                                )
                            }
                        }
                    )
                }


                /*
                 * アカウント
                 */
                4 -> {

                    AccountScreen()
                }
            }
        }
    }


    /*
     * =====================================
     * トイレ登録中
     * =====================================
     */
    if (
        isAddingToilet
    ) {

        AlertDialog(

            onDismissRequest = {
            },

            title = {

                Text(
                    text =
                        "トイレを登録中"
                )
            },

            text = {

                CircularProgressIndicator()
            },

            confirmButton = {
            }
        )
    }


    /*
     * =====================================
     * 口コミダイアログ
     * =====================================
     */
    val reviewTarget =
        selectedToilet


    if (
        showReviewDialog &&
        reviewTarget != null
    ) {

        ReviewDialog(

            toiletName =
                reviewTarget.name,

            reviews =
                reviews,

            isLoading =
                isLoadingReviews,

            isPosting =
                isPostingReview,

            currentUserId =
                reviewCurrentUserId,

            errorMessage =
                reviewErrorMessage,

            successMessage =
                reviewSuccessMessage,

            onReload = {

                onLoadReviews(
                    reviewTarget.id
                )
            },

            onSubmit = {
                    rating,
                    reviewComment ->


                onSubmitReview(

                    reviewTarget.id,

                    rating,

                    reviewComment
                )
            },

            onDelete = {
                    reviewId ->


                onDeleteReview(

                    reviewTarget.id,

                    reviewId
                )
            },

            onDismiss = {

                showReviewDialog =
                    false

                onClearReviewMessages()
            }
        )
    }
}