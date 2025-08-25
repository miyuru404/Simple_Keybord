package com.simple_keybord

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simple_keybord.ui.theme.Simple_keybordTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Simple_keybordTheme {
                MainPage()
            }
        }
    }
}

@Composable
fun MainPage() {
    val context = LocalContext.current

    val logo = painterResource(id = R.drawable.logo3)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Logo
        Image(
            painter = logo,
            contentDescription = "App Logo",
            modifier = Modifier
                .height(100.dp)
                .padding(bottom = 16.dp)
        )

        // Title
        Text(
            "Welcome to SnapKeys",
            fontSize = 24.sp,
            color = Color.Black
        )
        Text(
            "The Minimalistic Keyboard Experience",
            fontSize = 16.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Instructions
        Text("Step 1: Activate Keyboard", fontSize = 16.sp)
        Text("Step 2: Select Keyboard as Default", fontSize = 16.sp, modifier = Modifier.padding(bottom = 16.dp))

        // Enable keyboard button
        Button(
            onClick = { context.startActivity(Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS)) },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Enable Keyboard")
        }

        // Theme selection buttons
        Text("Choose Keyboard Theme:", fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MyKeyboardService.Companion.themes.keys.forEach { theme ->
                Button(
                    onClick = {
                        // Broadcast intent to notify keyboard of theme change
                        val intent = Intent(MyKeyboardService.ACTION_CHANGE_THEME)
                        intent.putExtra(MyKeyboardService.EXTRA_THEME_NAME, theme)
                        context.sendBroadcast(intent)
                    }
                ) {
                    Text(theme)
                }
            }
        }
    }
}
