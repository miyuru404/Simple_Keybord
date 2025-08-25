package com.simple_keybord

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun MainPage() {
    val context = LocalContext.current
    val themes = MyKeyboardService.Companion.themes
    var selectedTheme by remember { mutableStateOf("Light") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState), // vertical scrolling
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        val logo = painterResource(id = R.drawable.logo3)
        Image(
            painter = logo,
            contentDescription = "App Logo",
            modifier = Modifier
                .height(100.dp)
                .padding(bottom = 16.dp)
        )

        // Titles
        Text("Welcome to SnapKeys", fontSize = 24.sp, color = Color.Black)
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
            modifier = Modifier
                .padding(bottom = 24.dp)
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Enable Keyboard")
        }

        // Theme selection label
        Text("Choose Keyboard Theme:", fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))

        // Column of theme buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            themes.keys.forEach { themeName ->
                Button(
                    onClick = {
                        selectedTheme = themeName
                        val intent = Intent(MyKeyboardService.ACTION_CHANGE_THEME)
                        intent.putExtra(MyKeyboardService.EXTRA_THEME_NAME, selectedTheme)
                        context.sendBroadcast(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    border = if (selectedTheme == themeName) androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        color = Color.Blue
                    ) else null
                ) {
                    Text(text = themeName)
                }
            }
        }

        Spacer(modifier = Modifier.height(50.dp)) // extra spacing at bottom
    }
}



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
