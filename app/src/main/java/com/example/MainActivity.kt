package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferences
import com.example.data.repository.HisabRepository
import com.example.ui.navigation.MainAppNavigation
import com.example.ui.theme.HisabBoiTheme
import com.example.viewmodel.HisabViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: HisabViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()

            HisabBoiTheme(darkTheme = userSettings.isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainAppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
