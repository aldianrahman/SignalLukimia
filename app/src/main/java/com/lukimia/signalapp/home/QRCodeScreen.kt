package com.lukimia.signalapp.home

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.lukimia.signalapp.ui.theme.BlueAccent
import com.lukimia.signalapp.ui.theme.ButtonGray
import com.lukimia.signalapp.ui.theme.CardDark
import com.lukimia.signalapp.ui.theme.DarkNavy
import com.lukimia.signalapp.ui.theme.TextGray

@Composable
fun QRCodeScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    val auth = Firebase.auth
    val database = Firebase.database

    LaunchedEffect(auth.currentUser) {
        val user = auth.currentUser ?: return@LaunchedEffect
        val snapshot = database.getReference("users").child(user.uid).get().await()
        fullName = snapshot.child("fullName").getValue(String::class.java) ?: ""
        username = snapshot.child("username").getValue(String::class.java) ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextGray,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "Add Friend",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            // Spacer for balance
            Spacer(modifier = Modifier.size(48.dp))
        }

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = ButtonGray,
            contentColor = Color.White,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp)),
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = BlueAccent
                    )
                }
            },
            divider = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedTab == 0) BlueAccent else Color.Transparent)
            ) {
                Text(
                    text = "My Code",
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = if (selectedTab == 0) Color.White else TextGray,
                    fontWeight = FontWeight.Medium
                )
            }
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedTab == 1) BlueAccent else Color.Transparent)
            ) {
                Text(
                    text = "Scan",
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = if (selectedTab == 1) Color.White else TextGray,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (selectedTab) {
            0 -> MyCodeTab(fullName = fullName, username = username)
            1 -> ScanTab(navController = navController)
        }
    }
}

@Composable
private fun MyCodeTab(fullName: String, username: String) {
    val auth = Firebase.auth
    val userId = auth.currentUser?.uid ?: ""
    val qrBitmap = remember(userId) {
        if (userId.isNotEmpty()) generateQRCode(userId) else null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // QR Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardDark)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(BlueAccent.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = fullName.take(1).uppercase().ifEmpty { "?" },
                        color = BlueAccent,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = fullName.ifEmpty { "Loading..." },
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (username.isNotEmpty()) {
                        Text(
                            text = "@$username",
                            color = TextGray,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // QR Code
            if (qrBitmap != null) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading QR...", color = TextGray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Scan this code with your friend's\ncamera to instantly connect and start chatting.",
            color = TextGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Share Button
        Button(
            onClick = { /* TODO: Implement share */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Share My Profile Link", color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
private fun ScanTab(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Scan a friend's QR code",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Point your camera at a QR code to add a friend",
            color = TextGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate("scan") },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Open Camera", color = Color.White, fontSize = 16.sp)
        }
    }
}

private fun generateQRCode(content: String): Bitmap? {
    return try {
        val size = 512
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}
