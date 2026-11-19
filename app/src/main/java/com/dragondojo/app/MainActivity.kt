package com.dragondojo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dragondojo.app.data.DragonDojoViewModel
import com.dragondojo.app.data.OfflineDojoRepository
import com.dragondojo.app.ui.DragonDojoApp

class MainActivity : ComponentActivity() {
    private val viewModel: DragonDojoViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                DragonDojoViewModel(OfflineDojoRepository(applicationContext)) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.state.collectAsState()
            MaterialTheme(
                colorScheme = if (state.darkMode) darkColorScheme() else lightColorScheme()
            ) {
                DragonDojoApp(state, viewModel)
            }
        }
    }
}
