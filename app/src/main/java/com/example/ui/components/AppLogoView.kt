package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val LogoGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF148F9E),
        Color(0xFF0D6B77),
        Color(0xFF063E47)
    )
)

@Composable
fun AppLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    Surface(
        modifier = modifier
            .size(size)
            .shadow(10.dp, RoundedCornerShape(size * 0.28f)),
        shape = RoundedCornerShape(size * 0.28f),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(LogoGradient)
                .padding(size * 0.12f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(2.dp)
            ) {
                // Book Icon Graphic
                Box(
                    modifier = Modifier
                        .size(size * 0.52f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Page: Calculator & Payments
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Calculate,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(size * 0.18f)
                            )
                            Icon(
                                imageVector = Icons.Rounded.Payments,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(size * 0.16f)
                            )
                        }

                        // Spine separator
                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .height(size * 0.36f)
                                .background(Color.White.copy(alpha = 0.8f))
                        )

                        // Right Page: Taka & Growth
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "৳",
                                color = Color.White,
                                fontSize = (size.value * 0.2f).sp,
                                fontWeight = FontWeight.Black
                            )
                            Icon(
                                imageVector = Icons.Rounded.ShowChart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(size * 0.18f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Brand text
                Text(
                    text = "HisabBoi",
                    color = Color.White,
                    fontSize = (size.value * 0.13f).sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "হিসাববই",
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = (size.value * 0.10f).sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
