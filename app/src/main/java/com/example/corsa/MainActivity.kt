package com.example.corsa

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.corsa.ui.theme.CorsaTheme
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composeAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CorsaTheme {
                LoginScreen {
                    Log.i("Auth", "Login successful")
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val authState = supabase.composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Success -> {
                    errorMessage = null
                    onLoginSuccess()
                }
                is NativeSignInResult.ClosedByUser -> {
                    errorMessage = "Sign-in dismissed."
                }
                is NativeSignInResult.Error -> {
                    Log.e("Auth", "Error: ${result.message}")
                    errorMessage = "Error: ${result.message}"
                }
                is NativeSignInResult.NetworkError -> {
                    Log.e("Auth", "Network error: ${result.message}")
                    errorMessage = "Network error: ${result.message}"
                }
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { authState.startFlow() }) {
            Text("Sign in with Google")
        }

        errorMessage?.let { msg ->
            Text(
                text = msg,
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}