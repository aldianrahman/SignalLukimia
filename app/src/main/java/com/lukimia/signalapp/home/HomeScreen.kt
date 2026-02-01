package com.lukimia.signalapp.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun HomeScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var scannedBarcode by remember { mutableStateOf<String?>(null) }
    var friendRequests by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var friends by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    val auth = Firebase.auth
    val database = Firebase.database

    val barcode = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("barcode")

    if (barcode != null && !showDialog) {
        scannedBarcode = barcode
        showDialog = true
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Friend") },
            text = { Text("Do you want to add this user as a friend?") },
            confirmButton = {
                Button(onClick = {
                    val user = auth.currentUser
                    if (user != null && scannedBarcode != null) {
                        val friendRequestRef = database.getReference("friend_requests")
                            .child(scannedBarcode!!)
                            .child(user.uid)
                        friendRequestRef.setValue(true)
                    }
                    showDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    LaunchedEffect(auth.currentUser) {
        val user = auth.currentUser
        if (user != null) {
            val friendRequestRef = database.getReference("friend_requests").child(user.uid)
            friendRequestRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val requests = snapshot.children.mapNotNull {
                        val userId = it.key
                        val username = it.child("username").getValue(String::class.java)
                        if (userId != null && username != null) {
                            Pair(userId, username)
                        } else {
                            null
                        }
                    }
                    friendRequests = requests
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })

            val friendsRef = database.getReference("friends").child(user.uid)
            friendsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val friendIds = snapshot.children.mapNotNull { it.key }
                    val usersRef = database.getReference("users")
                    usersRef.get().addOnSuccessListener { usersSnapshot ->
                        val friendList = friendIds.mapNotNull { friendId ->
                            val username = usersSnapshot.child(friendId).child("username").getValue(String::class.java)
                            if (username != null) {
                                Pair(friendId, username)
                            } else {
                                null
                            }
                        }
                        friends = friendList
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Home Screen")
        Button(onClick = { navController.navigate("scan") }) {
            Text("Scan QR Code")
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            items(friendRequests) { request ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(request.second)
                    Button(onClick = {
                        val user = auth.currentUser
                        if (user != null) {
                            val friendsRef = database.getReference("friends")
                            friendsRef.child(user.uid).child(request.first).setValue(true)
                            friendsRef.child(request.first).child(user.uid).setValue(true)
                            val friendRequestRef = database.getReference("friend_requests")
                                .child(user.uid)
                                .child(request.first)
                            friendRequestRef.removeValue()
                        }
                    }) {
                        Text("Accept")
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            items(friends) { friend ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(friend.second)
                    Button(onClick = {
                        navController.navigate("chat/${friend.first}")
                    }) {
                        Text("Chat")
                    }
                }
            }
        }
    }
}