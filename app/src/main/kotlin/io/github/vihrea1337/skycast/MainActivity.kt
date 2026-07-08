package io.github.vihrea1337.skycast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import io.github.vihrea1337.skycast.core.designsystem.theme.SkyCastTheme
import io.github.vihrea1337.skycast.navigation.SkyCastNavHost

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkyCastTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SkyCastNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
