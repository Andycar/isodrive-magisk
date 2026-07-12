package com.nitanmarcel.isodriveui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.nitanmarcel.isodriveui.ui.MainScreen
import com.nitanmarcel.isodriveui.ui.theme.IsoDriveUITheme

class MainActivity : ComponentActivity() {

    private val viewModel: IsoDriveViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IsoDriveUITheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
