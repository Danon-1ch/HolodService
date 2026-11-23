package ru.tisbi.servicecenter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.tisbi.servicecenter.ui.AppNav
import ru.tisbi.servicecenter.ui.theme.HolodServiceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HolodServiceTheme {
                AppNav()
            }
        }
    }
}
