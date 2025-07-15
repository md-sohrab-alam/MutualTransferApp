package com.shikshak.transfer.ui.theme.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(durationMillis = 1000)
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(3000L)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Container with rounded corners
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF5F5DC)) // Light beige background
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Circular logo
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    // Plant/Wheat stalk icon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Main stem
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(40.dp)
                                .background(Color(0xFF2E7D32)) // Dark green
                        )
                        
                        // Leaves/branches
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.width(60.dp)
                        ) {
                            // Left leaf
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .height(8.dp)
                                    .background(Color(0xFF4CAF50)) // Light green
                            )
                            
                            // Right leaf
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .height(8.dp)
                                    .background(Color(0xFF4CAF50)) // Light green
                            )
                        }
                        
                        // Top leaves
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.width(40.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(8.dp)
                                    .height(6.dp)
                                    .background(Color(0xFF4CAF50))
                            )
                            Box(
                                modifier = Modifier
                                    .width(8.dp)
                                    .height(6.dp)
                                    .background(Color(0xFF4CAF50))
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App Title
            Text(
                text = "MutualTransfer Bihar",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242), // Dark grey
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subtitle
            Text(
                text = "Connecting Teachers Across Districts",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF666666), // Medium grey
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha)
            )
            
            Spacer(modifier = Modifier.height(80.dp))
            
            // Footer
            Text(
                text = "Powered by Open Collaboration",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF999999), // Light grey
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha)
            )
        }
    }
} 