package com.example.a2026scoutingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.a2026scoutingapp.ui.AppRoot
import com.example.a2026scoutingapp.ui.theme._2026ScoutingAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            _2026ScoutingAppTheme {
                AppRoot()
            }
        }
    }
}
