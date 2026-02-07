package com.lukimia.signalapp.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.lukimia.signalapp.MainActivity
import com.lukimia.signalapp.R
import com.lukimia.signalapp.ui.theme.SignalAppTheme

class OnboardingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            SignalAppTheme {
                OnboardingScreen(
                    onFinish = { navigateToMain() }
                )
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

private data class OnboardingPage(
    val icon: Int,
    val title: Int,
    val description: Int
)

private val onboardingPages = listOf(
    OnboardingPage(
        icon = R.drawable.ic_briefcase,
        title = R.string.onboarding_title_1,
        description = R.string.onboarding_desc_1
    ),
    OnboardingPage(
        icon = R.drawable.ic_chat_bubble,
        title = R.string.onboarding_title_2,
        description = R.string.onboarding_desc_2
    ),
    OnboardingPage(
        icon = R.drawable.ic_document_sign,
        title = R.string.onboarding_title_3,
        description = R.string.onboarding_desc_3
    )
)

@Composable
private fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })

    val DarkNavy = Color(0xFF0E1621)
    val BluePrimary = Color(0xFF3A82F8)
    val TextGray = Color(0xFF8A99AD)
    val IconBg = Color(0x1A3A82F8)
    val DotInactive = Color(0x4D8A99AD)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        // Background gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC0E1621),
                            Color(0xE60E1621),
                            Color(0xFF0E1621)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, bottom = 40.dp)
        ) {
            // Skip button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = stringResource(R.string.lewati),
                    color = TextGray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { onFinish() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val item = onboardingPages[page]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.weight(0.3f))

                    // Icon container
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(IconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = item.icon),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Title
                    Text(
                        text = stringResource(item.title),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    Text(
                        text = stringResource(item.description),
                        color = TextGray,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.weight(0.5f))
                }
            }

            // Page indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(onboardingPages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 8.dp,
                        animationSpec = tween(300),
                        label = "indicator_width"
                    )
                    val color by animateColorAsState(
                        targetValue = if (isSelected) BluePrimary else DotInactive,
                        animationSpec = tween(300),
                        label = "indicator_color"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(width)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            // Get Started button
            Button(
                onClick = { onFinish() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = 24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.mulai_sekarang),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_forward),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
