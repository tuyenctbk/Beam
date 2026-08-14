package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BeamPrimary

data class QuickTipStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val badgeText: String
)

@Composable
fun HelpScreen(
    ipAddress: String,
    port: Int,
    wifiSsid: String,
    onNavigateToBeam: () -> Unit
) {
    val steps = remember {
        listOf(
            QuickTipStep(
                stepNumber = 1,
                title = "Connect TV & Mobile to Same Wi-Fi",
                description = "Ensure your Android TV and phone/computer are connected to $wifiSsid. Local network connection is required for high-speed offline transfers.",
                icon = Icons.Default.Wifi,
                badgeText = "STEP 1"
            ),
            QuickTipStep(
                stepNumber = 2,
                title = "Scan QR Code or Open Web Portal URL",
                description = "Scan the QR code on the BEAM tab using your phone's camera, or type http://$ipAddress:$port into any browser (Chrome, Safari, Edge).",
                icon = Icons.Default.QrCode,
                badgeText = "STEP 2"
            ),
            QuickTipStep(
                stepNumber = 3,
                title = "Drag & Drop Files or Select APKs",
                description = "On the web portal interface, drag and drop photos, videos, movies, ZIP files, or Android APKs. Files stream directly to your TV at full Wi-Fi speeds.",
                icon = Icons.Default.Send,
                badgeText = "STEP 3"
            ),
            QuickTipStep(
                stepNumber = 4,
                title = "Remote Keyboard & Clipboard Sync",
                description = "Paste URLs, passwords, or search queries into the web portal text field. The text is instantly copied to your TV clipboard for easy remote input.",
                icon = Icons.Default.Keyboard,
                badgeText = "STEP 4"
            ),
            QuickTipStep(
                stepNumber = 5,
                title = "Install APKs & Extract ZIPs on TV",
                description = "Switch to the FILES or APKS tab using your TV remote D-pad. Tap any downloaded APK to install it with 1-click, or uncompress ZIP archives directly.",
                icon = Icons.Default.Android,
                badgeText = "STEP 5"
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .testTag("help_quick_tips_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Header Banner with gorgeous Canvas-drawn ambient glow and signal waves
            var isHeaderFocused by remember { mutableStateOf(false) }
            val headerScale by animateFloatAsState(
                targetValue = if (isHeaderFocused) 1.01f else 1.0f,
                animationSpec = spring(dampingRatio = 0.85f, stiffness = 200f),
                label = "headerScale"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(scaleX = headerScale, scaleY = headerScale)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E293B), // Slate 800
                                Color(0xFF0F172A), // Slate 900
                                Color(0xFF1E1B4B)  // Deep Indigo
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (isHeaderFocused) Color(0xFF38BDF8) else Color(0xFF334155),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .onFocusChanged { isHeaderFocused = it.isFocused }
                    .focusable()
                    .clickable { onNavigateToBeam() }
                    .padding(24.dp)
            ) {
                // Background Canvas drawing glowing wireless radar arcs
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    val width = size.width
                    val height = size.height
                    
                    // Draw glowing concentric wireless arcs in the bottom-right corner
                    val strokeColor = Color(0xFF38BDF8).copy(alpha = 0.08f)
                    val strokeWidth = 2.dp.toPx()
                    
                    drawCircle(
                        color = Color(0xFF332563EB).copy(alpha = 0.15f),
                        radius = 200.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(width * 0.9f, height * 0.5f)
                    )
                    
                    drawCircle(
                        color = strokeColor,
                        radius = 120.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(width * 0.9f, height * 0.5f),
                        style = Stroke(width = strokeWidth)
                    )
                    
                    drawCircle(
                        color = strokeColor,
                        radius = 160.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(width * 0.9f, height * 0.5f),
                        style = Stroke(width = strokeWidth)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Help Guide",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Quick Connection Guide",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Learn how to send files wirelessly to your Android TV in 5 easy steps. Tap to show connection QR code.",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isHeaderFocused) Color(0xFF38BDF8) else Color(0xFF2563EB))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = if (isHeaderFocused) Color(0xFF0F172A) else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Go to QR Code",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHeaderFocused) Color(0xFF0F172A) else Color.White
                            )
                        }
                    }
                }
            }
        }

        itemsIndexed(steps) { index, step ->
            var isFocused by remember { mutableStateOf(false) }

            // Spring scale effect on D-pad focused cards
            val scale by animateFloatAsState(
                targetValue = if (isFocused) 1.02f else 1.0f,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                label = "stepScale"
            )

            val borderColor = if (isFocused) Color(0xFF38BDF8) else MaterialTheme.colorScheme.outline
            val containerBg = if (isFocused) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .clip(RoundedCornerShape(20.dp))
                    .background(containerBg)
                    .border(2.dp, borderColor, RoundedCornerShape(20.dp))
                    .onFocusChanged { isFocused = it.isFocused }
                    .focusable()
                    .padding(20.dp)
                    .testTag("help_step_card_$index")
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isFocused) Color(0xFF1E293B) else MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = step.title,
                            tint = if (isFocused) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = step.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isFocused) Color(0xFF38BDF8) else Color(0xFF1E293B))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = step.badgeText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFocused) Color(0xFF0F172A) else Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = step.description,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
