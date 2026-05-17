package com.vaastuverse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vaastuverse.app.ui.VaastuVerseTheme
import com.vaastuverse.app.ui.RootShellScreen
import com.vaastuverse.app.data.UserSessionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VaastuVerseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val session: UserSessionViewModel = viewModel()
                    RootShellScreen(session = session)
                }
            }
        }
    }
}
