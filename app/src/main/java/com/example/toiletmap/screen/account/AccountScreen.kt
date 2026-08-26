package com.example.toiletmap.screen.account

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.toiletmap.data.repository.AccountRepository
import com.example.toiletmap.model.UserProfile


/*
 * アカウント画面内で
 * どのページを表示しているか
 */
private enum class AccountPage {

    PROFILE,

    POINT_EXCHANGE,

    POINT_EXCHANGE_HISTORY
}


@Composable
fun AccountScreen() {

    var isLoggedIn by remember {

        mutableStateOf(
            AccountRepository
                .isLoggedIn()
        )
    }


    var page by remember {

        mutableStateOf(
            AccountPage.PROFILE
        )
    }


    /*
     * =========================================
     * ログイン済み
     * =========================================
     */
    if (isLoggedIn) {

        when (page) {

            /*
             * =====================================
             * アカウント
             * =====================================
             */
            AccountPage.PROFILE -> {

                ProfileScreen(

                    onLogout = {

                        isLoggedIn =
                            false

                        page =
                            AccountPage.PROFILE
                    },

                    onOpenPointExchange = {

                        page =
                            AccountPage.POINT_EXCHANGE
                    },

                    onOpenPointExchangeHistory = {

                        page =
                            AccountPage.POINT_EXCHANGE_HISTORY
                    }
                )
            }


            /*
             * =====================================
             * ポイント交換
             * =====================================
             */
            AccountPage.POINT_EXCHANGE -> {

                PointExchangeAccountPage(

                    onBack = {

                        page =
                            AccountPage.PROFILE
                    }
                )
            }


            /*
             * =====================================
             * ポイント交換履歴
             * =====================================
             */
            AccountPage.POINT_EXCHANGE_HISTORY -> {

                PointExchangeHistoryScreen(

                    onBack = {

                        page =
                            AccountPage.PROFILE
                    }
                )
            }
        }

    } else {

        /*
         * =========================================
         * 未ログイン
         * =========================================
         */
        LoginAndRegisterScreen(

            onLoginSuccess = {

                isLoggedIn =
                    true

                page =
                    AccountPage.PROFILE
            }
        )
    }
}


/*
 * =============================================
 * ポイント交換画面を開く前に
 * 最新の清掃報酬ポイントを取得する
 * =============================================
 */
@Composable
private fun PointExchangeAccountPage(
    onBack: () -> Unit
) {

    val user =
        AccountRepository
            .getCurrentUser()


    var profile by remember(
        user?.id
    ) {

        mutableStateOf<UserProfile?>(
            null
        )
    }


    var loading by remember(
        user?.id
    ) {

        mutableStateOf(
            true
        )
    }


    LaunchedEffect(
        user?.id
    ) {

        loading =
            true


        try {

            if (user != null) {

                profile =
                    AccountRepository
                        .loadProfile(
                            user.id
                        )
            }

        } catch (e: Exception) {

            e.printStackTrace()

        } finally {

            loading =
                false
        }
    }


    if (loading) {

        Box(
            modifier =
                Modifier.fillMaxSize(),
            contentAlignment =
                Alignment.Center
        ) {

            CircularProgressIndicator()
        }

    } else {

        PointExchangeScreen(

            /*
             * 商品交換に使用するのは
             * 清掃報酬ポイント
             */
            currentPoints =
                profile
                    ?.rewardPoints
                    ?: 0,

            onBack =
                onBack
        )
    }
}