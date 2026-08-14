package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BeamOnBackground
import com.example.ui.theme.BeamOnPrimaryContainer
import com.example.ui.theme.BeamPrimary
import com.example.ui.theme.BeamPrimaryContainer
import com.example.ui.theme.BeamSecondary

@Composable
fun QrCardSection(
    wifiSsid: String,
    ipAddress: String,
    port: Int,
    qrBitmap: ImageBitmap?,
    isNetworkAvailable: Boolean = true
) {
    val fullUrl = if (isNetworkAvailable && ipAddress != "127.0.0.1") "http://$ipAddress:$port" else "http://[Connect Wi-Fi]:$port"

    // Infinite pulsing animation for QR code visual prominence
    val infiniteTransition = rememberInfiniteTransition(label = "QrPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    var isQrFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Network Status Badge Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isNetworkAvailable) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                .border(
                    width = 1.dp,
                    color = if (isNetworkAvailable) Color(0xFF86EFAC) else Color(0xFFFCA5A5),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = if (isNetworkAvailable) Icons.Default.Wifi else Icons.Default.WifiOff,
                contentDescription = null,
                tint = if (isNetworkAvailable) Color(0xFF15803D) else Color(0xFFB91C1C),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isNetworkAvailable) "Wi-Fi: $wifiSsid" else "No Wi-Fi Connection",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isNetworkAvailable) Color(0xFF15803D) else Color(0xFFB91C1C)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isNetworkAvailable) stringResource(R.string.ready_to_receive) else stringResource(R.string.local_network_offline),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isNetworkAvailable) stringResource(R.string.scan_qr_desc, wifiSsid) else stringResource(R.string.connect_wifi_prompt),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // QR Code Card with glowing pulsing ambient blur effect
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.testTag("qr_card_container")
        ) {
            // Glow backdrop
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .graphicsLayer {
                        scaleX = if (isQrFocused) 1.08f else pulseScale
                        scaleY = if (isQrFocused) 1.08f else pulseScale
                    }
                    .clip(RoundedCornerShape(40.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = if (isQrFocused) 0.6f else pulseAlpha),
                                Color.Transparent
                            )
                        )
                    )
                    .blur(20.dp)
            )

            // QR Card Surface
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .graphicsLayer {
                        scaleX = if (isQrFocused) 1.05f else pulseScale
                        scaleY = if (isQrFocused) 1.05f else pulseScale
                    }
                    .shadow(
                        elevation = if (isQrFocused) 24.dp else 12.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White)
                    .border(
                        width = if (isQrFocused) 3.dp else 1.5.dp,
                        color = if (isQrFocused) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .onFocusChanged { isQrFocused = it.isFocused }
                    .focusable()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isNetworkAvailable && qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap,
                        contentDescription = "Scan QR Code to beam files",
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("qr_code_image")
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = BeamSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (!isNetworkAvailable) stringResource(R.string.connect_wifi_prompt) else stringResource(R.string.loading),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = BeamSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // URL Chip Badge (Focusable on Android TV with remote D-Pad)
        var isUrlFocused by remember { mutableStateOf(false) }
        val context = LocalContext.current

        val urlBorderColor = if (isUrlFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        val urlBg = if (isUrlFocused) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        val urlTextColor = if (isUrlFocused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.visit_url_browser),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = if (isUrlFocused) 1.05f else 1.0f
                        scaleY = if (isUrlFocused) 1.05f else 1.0f
                    }
                    .shadow(
                        elevation = if (isUrlFocused) 8.dp else 2.dp,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(urlBg)
                    .border(2.dp, urlBorderColor, RoundedCornerShape(16.dp))
                    .onFocusChanged { isUrlFocused = it.isFocused }
                    .focusable()
                    .clickable {
                        try {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Beam URL", fullUrl)
                            clipboard.setPrimaryClip(clip)
                            com.example.util.FirebaseManager.logUserAction("copy_url_to_clipboard", fullUrl)
                        } catch (e: Exception) {
                            com.example.util.FirebaseManager.recordException(e)
                        }
                    }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .testTag("ip_url_badge")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = fullUrl,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = urlTextColor
                    )
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy URL",
                        tint = urlTextColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (isUrlFocused) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.press_to_copy),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
