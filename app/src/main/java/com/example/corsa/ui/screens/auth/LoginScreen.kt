package com.example.corsa.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { LoginScreenTopBar(onBack = { navController.popBackStack() }) }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = "WELCOME BACK!\n",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                fontSize = 56.sp,
                lineHeight = 58.sp,
                textAlign = TextAlign.Center,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fields
                Spacer(modifier = Modifier.height(8.dp))
                EmailField(email = email, onEmailChange = { email = it })
                PasswordField(
                    password = password,
                    onPasswordChange = { password = it },
                    passwordVisible = passwordVisible,
                    onToggleVisibility = { passwordVisible = !passwordVisible }
                )
                // Buttons
                LoginDivider()
                GoogleButton(onClick = { /* viewModel.loginWithGoogle() */ })
                LoginButton(onClick = { /* viewModel.loginWithEmail(email, password) */ })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreenTopBar(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Korsa",
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                fontSize = 28.sp,
                letterSpacing = 2.sp,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    )
}

@Composable
private fun EmailField(email: String, onEmailChange: (String) -> Unit) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    )
}

@Composable
private fun PasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit,
) {
    val visibilityIcon: ImageVector =
        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
    val visibilityDesc = if (passwordVisible) "Hide password" else "Show password"

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        singleLine = true,
        visualTransformation =
            if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(imageVector = visibilityIcon, contentDescription = visibilityDesc)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    )
}

@Composable
private fun LoginButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(
            text = "Log In",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            letterSpacing = 1.sp,
        )
    }
}

@Composable
private fun GoogleButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(
            text = "Continue with Google",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            letterSpacing = 1.sp,
        )
    }
}

@Composable
private fun LoginDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = "or",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}