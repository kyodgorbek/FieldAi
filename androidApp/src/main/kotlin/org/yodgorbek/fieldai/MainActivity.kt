package org.yodgorbek.fieldai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.yodgorbek.fieldai.ui.navigation.AppNavigation
import org.yodgorbek.fieldai.ui.theme.FieldAiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            FieldAiTheme {
                AppNavigation()
            }
        }
    }
}