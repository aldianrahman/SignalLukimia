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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
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
import com.lukimia.signalapp.ui.theme.CardDark
import com.lukimia.signalapp.ui.theme.DarkNavy
import com.lukimia.signalapp.ui.theme.SearchBarColor
import com.lukimia.signalapp.ui.theme.TextGray

data class FriendRequest(
    val userId: String,
    val name: String,
    val username: String
)

@Composable
fun SocialScreen(navController: NavController, modifier: Modifier = Modifier) {
    var searchQuery by remember { mutableStateOf("") }
    var friendRequests by remember { mutableStateOf<List<FriendRequest>>(emptyList()) }
    var friends by remember { mutableStateOf<List<FriendRequest>>(emptyList()) }
    val auth = Firebase.auth
    val database = Firebase.database

    LaunchedEffect(auth.currentUser) {
        val user = auth.currentUser ?: return@LaunchedEffect

        // Listen for friend requests
        val friendRequestRef = database.getReference("friend_requests").child(user.uid)
        friendRequestRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requestIds = snapshot.children.mapNotNull { it.key }
                if (requestIds.isEmpty()) {
                    friendRequests = emptyList()
                    return
                }
                val usersRef = database.getReference("users")
                usersRef.get().addOnSuccessListener { usersSnapshot ->
                    friendRequests = requestIds.mapNotNull { userId ->
                        val name = usersSnapshot.child(userId).child("fullName")
                            .getValue(String::class.java) ?: "Unknown"
                        val username = usersSnapshot.child(userId).child("username")
                            .getValue(String::class.java) ?: ""
                        FriendRequest(userId, name, username)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // Listen for friends list
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
                            .getValue(String::class.java) ?: "Unknown"
                        val username = usersSnapshot.child(friendId).child("username")
                            .getValue(String::class.java) ?: ""
                        FriendRequest(friendId, name, username)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Social",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { navController.navigate("qrcode") }) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = "QR Code",
                        tint = BlueAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Search Bar
        item {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by username or ID", color = TextGray, fontSize = 14.sp) },
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
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Pending Requests Section
        if (friendRequests.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pending Requests",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(BlueAccent)
                            .size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${friendRequests.size}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(friendRequests) { request ->
                FriendRequestCard(
                    request = request,
                    onAccept = {
                        val user = auth.currentUser ?: return@FriendRequestCard
                        val friendsRef = database.getReference("friends")
                        friendsRef.child(user.uid).child(request.userId).setValue(true)
                        friendsRef.child(request.userId).child(user.uid).setValue(true)
                        database.getReference("friend_requests")
                            .child(user.uid)
                            .child(request.userId)
                            .removeValue()
                    },
                    onDecline = {
                        val user = auth.currentUser ?: return@FriendRequestCard
                        database.getReference("friend_requests")
                            .child(user.uid)
                            .child(request.userId)
                            .removeValue()
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // Friends Section
        item {
            Text(
                text = "Friends",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (friends.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No friends yet",
                        color = TextGray,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scan a QR code or share yours to add friends!",
                        color = TextGray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            val filteredFriends = if (searchQuery.isBlank()) friends
            else friends.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.username.contains(searchQuery, ignoreCase = true)
            }

            items(filteredFriends) { friend ->
                FriendListItem(friend = friend, onClick = {
                    navController.navigate("chat/${friend.userId}")
                })
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun FriendRequestCard(
    request: FriendRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(BlueAccent.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = request.name.take(1).uppercase(),
                color = BlueAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name and info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = request.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (request.username.isNotEmpty()) {
                Text(
                    text = "@${request.username}",
                    color = TextGray,
                    fontSize = 13.sp
                )
            }
        }

        // Decline Button
        IconButton(
            onClick = onDecline,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF3A3A3A))
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Decline",
                tint = TextGray,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Accept Button
        IconButton(
            onClick = onAccept,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(BlueAccent)
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Accept",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun FriendListItem(friend: FriendRequest, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(BlueAccent.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = friend.name.take(1).uppercase(),
                color = BlueAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = friend.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (friend.username.isNotEmpty()) {
                Text(
                    text = "@${friend.username}",
                    color = TextGray,
                    fontSize = 13.sp
                )
            }
        }
    }
}
