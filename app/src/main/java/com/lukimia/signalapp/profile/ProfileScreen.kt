package com.lukimia.signalapp.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun ProfileScreen(navController: NavController) {
    val auth = Firebase.auth
    val database = Firebase.database
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    val completeRegistration: () -> Unit = {
        val user = auth.currentUser
        if (user != null) {
            val userRef = database.getReference("users").child(user.uid)
            val userData = mapOf(
                "fullName" to fullName,
                "username" to username,
                "bio" to bio
            )
            userRef.setValue(userData).addOnCompleteListener {
                navController.navigate("home") {
                    popUpTo("profile") { inclusive = true }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") }
        )
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )
        TextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Bio") }
        )
        Button(onClick = completeRegistration) {
            Text("Complete Registration")
        }
    }
}