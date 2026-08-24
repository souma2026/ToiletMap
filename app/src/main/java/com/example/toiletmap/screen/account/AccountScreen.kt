package com.example.toiletmap.screen.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.toiletmap.data.repository.AccountRepository


@Composable
fun AccountScreen() {

    var isLoggedIn by remember {

        mutableStateOf(
            AccountRepository.isLoggedIn()
        )
    }


    if (isLoggedIn) {

        ProfileScreen(
            onLogout = {

                isLoggedIn = false
            }
        )

    } else {

        LoginAndRegisterScreen(
            onLoginSuccess = {

                isLoggedIn = true
            }
        )
    }
}