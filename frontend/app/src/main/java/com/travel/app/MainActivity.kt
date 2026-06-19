package com.travel.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.travel.app.data.AppContainer
import com.travel.app.data.repository.UserRepositoryImpl
import com.travel.app.presentation.app.TravelApp
import com.travel.app.presentation.auth.AuthViewModel
import com.travel.app.presentation.auth.LoginScreen
import com.travel.app.presentation.auth.RegisterScreen
import com.travel.app.presentation.theme.TravelTheme
import com.travel.app.service.ApiService

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelTheme {
                TravelApp()
            }
        }
    }
}
