package com.lukimia.signalapp.chat

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize

@Composable
fun ChatScreen(navController: NavController, chatId: String?) {
    var messages by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var newMessage by remember { mutableStateOf("") }
    val auth = Firebase.auth
    val database = Firebase.database

    DisposableEffect(chatId) {
        if (chatId == null) return@DisposableEffect onDispose { }
        val chatRef = database.getReference("chats").child(chatId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messageList = snapshot.children.mapNotNull {
                    val sender = it.child("sender").getValue(String::class.java)
                    val text = it.child("text").getValue(String::class.java)
                    if (sender != null && text != null) {
                        Pair(sender, text)
                    } else {
                        null
                    }
                }
                messages = messageList
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }
        chatRef.addValueEventListener(listener)
        onDispose {
            chatRef.removeEventListener(listener)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            items(messages) { message ->
                Text("${message.first}: ${message.second}")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                val user = auth.currentUser
                if (user != null && chatId != null) {
                    val chatRef = database.getReference("chats").child(chatId)
                    val messageData = mapOf(
                        "sender" to user.uid,
                        "text" to newMessage
                    )
                    chatRef.push().setValue(messageData)
                    newMessage = ""
                }
            }) {
                Text("Send")
            }
        }
    }
}