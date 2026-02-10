package com.lukimia.signalapp.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.lukimia.signalapp.ui.theme.BlueAccent
import com.lukimia.signalapp.ui.theme.DarkNavy
import com.lukimia.signalapp.ui.theme.DividerColor
import com.lukimia.signalapp.ui.theme.SearchBarColor
import com.lukimia.signalapp.ui.theme.TextGray

data class ChatItem(
    val friendId: String,
    val name: String,
    val lastMessage: String,
    val time: String
)

@Composable
fun ChatListScreen(navController: NavController, modifier: Modifier = Modifier) {
    var searchQuery by remember { mutableStateOf("") }
    var friends by remember { mutableStateOf<List<ChatItem>>(emptyList()) }
    val auth = Firebase.auth
    val database = Firebase.database

    LaunchedEffect(auth.currentUser) {
        val user = auth.currentUser ?: return@LaunchedEffect
        val friendsRef = database.getReference("friends").child(user.uid)
        friendsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val friendIds = snapshot.children.mapNotNull { it.key }
                if (friendIds.isEmpty()) {
                    friends = emptyList()
                    return
                }
                val usersRef = database.getReference("users")
                usersRef.get().addOnSuccessListener { usersSnapshot ->
                    friends = friendIds.mapNotNull { friendId ->
                        val name = usersSnapshot.child(friendId).child("fullName")
                            .getValue(String::class.java)
                            ?: usersSnapshot.child(friendId).child("username")
                                .getValue(String::class.java)
                        if (name != null) {
                            ChatItem(
                                friendId = friendId,
                                name = name,
                                lastMessage = "Tap to start chatting",
                                time = ""
                            )
                        } else null
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Chats",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Row {
                IconButton(onClick = { navController.navigate("qrcode") }) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = "QR Code",
                        tint = BlueAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = { navController.navigate("qrcode") }) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = "Add Friend",
                        tint = BlueAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Search Bar
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search conversations...", color = TextGray, fontSize = 14.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SearchBarColor,
                unfocusedContainerColor = SearchBarColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = BlueAccent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Chat List
        if (friends.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No chats yet",
                    color = TextGray,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add friends to start chatting!",
                    color = TextGray,
                    fontSize = 14.sp
                )
            }
        } else {
            val filteredFriends = if (searchQuery.isBlank()) friends
            else friends.filter { it.name.contains(searchQuery, ignoreCase = true) }

            LazyColumn {
                items(filteredFriends) { chat ->
                    ChatListItem(chat = chat, onClick = {
                        navController.navigate("chat/${chat.friendId}")
                    })
                    Divider(
                        color = DividerColor,
                        modifier = Modifier.padding(start = 80.dp, end = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatListItem(chat: ChatItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(BlueAccent.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chat.name.take(1).uppercase(),
                    color = BlueAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name and message
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chat.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chat.lastMessage,
                    color = TextGray,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }

            // Time
            if (chat.time.isNotEmpty()) {
                Text(
                    text = chat.time,
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
        }
    }

