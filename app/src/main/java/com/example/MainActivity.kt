package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable Edge-to-Edge full bleed layout support (Material You / M3)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val mainViewModel: AmbunalGoViewModel = viewModel()
                    AmbunalGoMainApp(viewModel = mainViewModel)
                }
            }
        }
    }
}

@Composable
fun AmbunalGoMainApp(viewModel: AmbunalGoViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Handle System Back presses seamlessly to provide premium user experience
    BackHandler(enabled = currentScreen != AppScreen.LOGIN) {
        when (currentScreen) {
            AppScreen.HOME -> viewModel.handleLogout()
            AppScreen.BOOKING, AppScreen.SOS, AppScreen.EVENT, AppScreen.HISTORY -> viewModel.navigateTo(AppScreen.HOME)
            else -> {}
        }
    }

    // High fidelity transition animations between state screens
    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "screen_routing"
    ) { screen ->
        when (screen) {
            AppScreen.LOGIN -> LoginScreen(viewModel = viewModel)
            AppScreen.HOME -> HomeScreen(viewModel = viewModel)
            AppScreen.BOOKING -> BookingMapScreen(viewModel = viewModel)
            AppScreen.SOS -> SosScreen(viewModel = viewModel)
            AppScreen.EVENT -> EventBookingScreen(viewModel = viewModel)
            AppScreen.HISTORY -> HistoryScreen(viewModel = viewModel)
        }
    }
}
