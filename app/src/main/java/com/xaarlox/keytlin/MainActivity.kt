package com.xaarlox.keytlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.xaarlox.keytlin.ui.navigation.AppNavigation
import com.xaarlox.keytlin.ui.theme.KeytlinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KeytlinTheme {
                AppNavigation()
            }
        }
    }
}