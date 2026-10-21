package com.numbloo.mathfun

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.numbloo.mathfun.ui.NumblooApp
import com.numbloo.mathfun.ui.theme.NumblooTheme

class MainActivity : ComponentActivity() {

    private lateinit var app: AppState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        app = AppState(applicationContext)
        setContent {
            NumblooTheme {
                NumblooApp(app)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        app.hush()
    }

    override fun onDestroy() {
        super.onDestroy()
        app.shutdown()
    }
}
