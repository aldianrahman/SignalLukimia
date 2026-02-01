package com.lukimia.signalapp.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.lukimia.signalapp.R
import com.lukimia.signalapp.ui.theme.BlueGradientEnd
import com.lukimia.signalapp.ui.theme.BlueGradientStart
import com.lukimia.signalapp.ui.theme.ButtonGray
import com.lukimia.signalapp.ui.theme.DarkNavy
import com.lukimia.signalapp.ui.theme.TextGray
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val googleAuthUiClient = GoogleAuthUiClient(context)
    val database = Firebase.database

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        scope.launch {
            googleAuthUiClient.signInWithIntent(result.data ?: return@launch)
            val user = googleAuthUiClient.getSignedInUser()
            if (user != null) {
                val userRef = database.getReference("users").child(user.uid)
                val snapshot = userRef.get().await()
                if (snapshot.exists()) {
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                } else {
                    navController.navigate("profile") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Placeholder for the logo - replace with your actual logo
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Ganti dengan logo Anda
            contentDescription = "Signal Logo",
            modifier = Modifier
                .size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to Signal",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "The most secure way to connect\nwith your world.",
            color = TextGray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Google Sign-In Button
        Button(
            onClick = {
                scope.launch {
                    launcher.launch(googleAuthUiClient.signIn())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ButtonGray),
            shape = RoundedCornerShape(8.dp)
        ) {
            // Placeholder for Google icon
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Ganti dengan ikon Google
                contentDescription = "Google Icon",
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Continue with Google", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Use phone number button
        Button(
            onClick = { /* TODO: Handle phone number sign in */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(BlueGradientStart, BlueGradientEnd)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Use phone number", color = Color.White, fontSize = 16.sp)
        }

        // ... (rest of the UI)

        Spacer(modifier = Modifier.weight(1f))
    }
}