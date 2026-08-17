package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.core.navigation.RamisaNavGraph
import com.example.ui.theme.RamisaTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      RamisaTheme {
        val navController = rememberNavController()
        Surface(modifier = Modifier.fillMaxSize()) {
          RamisaNavGraph(navController = navController)
        }
      }
    }
  }
}

