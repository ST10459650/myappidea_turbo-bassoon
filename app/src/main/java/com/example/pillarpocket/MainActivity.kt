package com.example.pillarpocket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.pillarpocket.ui.navigation.NavGraph
import com.example.pillarpocket.ui.theme.PillarPocketTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PillarPocketTheme {
                NavGraph()
            }
        }
    }
}