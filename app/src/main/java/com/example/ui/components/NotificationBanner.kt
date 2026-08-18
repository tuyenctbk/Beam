package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SdCardAlert
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BeamError
import com.example.ui.theme.BeamErrorBg
import com.example.ui.theme.BeamOnPrimaryContainer
import com.example.ui.theme.BeamPrimary
import com.example.ui.theme.BeamPrimaryContainer
import com.example.ui.theme.BeamWarning
import com.example.ui.theme.BeamWarningBg
import kotlinx.coroutines.delay

enum class TvToastType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO,
    TRANSFER,
    DELETE,
    NETWORK
}

data class TvToast(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val message: String,
    val type: TvToastType = TvToastType.INFO,
    val durationMs: Long = 4500L
)

@Composable
fun TvToastNotification(
    toast: TvToast?,
    onDismiss: () -> Unit
) {
    LaunchedEffect(toast?.id) {
        if (toast != null) {
            delay(toast.durationMs)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = toast != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn() + scaleIn(initialScale = 0.9f),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut() + scaleOut(targetScale = 0.9f)
    ) {
        if (toast != null) {
            val (bgColor, borderColor, iconBgColor, iconTint, icon) = when (toast.type) {
                TvToastType.TRANSFER -> ToastStyle(
                    bg = Color(0xFF0F291E),
                    border = Color(0xFF10B981),
                    iconBg = Color(0xFF10B981),
                    iconTint = Color.White,
                    icon = Icons.Default.CheckCircle
                )
                TvToastType.SUCCESS -> ToastStyle(
                    bg = Color(0xFF0F291E),
                    border = Color(0xFF10B981),
                    iconBg = Color(0xFF10B981),
                    iconTint = Color.White,
                    icon = Icons.Default.CheckCircle
                )
                TvToastType.ERROR -> ToastStyle(
                    bg = Color(0xFF2D1214),
                    border = Color(0xFFEF4444),
                    iconBg = Color(0xFFEF4444),
                    iconTint = Color.White,
                    icon = Icons.Default.ErrorOutline
                )
                TvToastType.WARNING -> ToastStyle(
                    bg = Color(0xFF2E2009),
                    border = Color(0xFFF59E0B),
                    iconBg = Color(0xFFF59E0B),
                    iconTint = Color.Black,
                    icon = Icons.Default.SdCardAlert
                )
                TvToastType.NETWORK -> ToastStyle(
                    bg = Color(0xFF2E1B09),
                    border = Color(0xFFF97316),
                    iconBg = Color(0xFFF97316),
                    iconTint = Color.White,
                    icon = Icons.Default.WifiOff
                )
                TvToastType.DELETE -> ToastStyle(
                    bg = Color(0xFF1E293B),
                    border = Color(0xFF94A3B8),
                    iconBg = Color(0xFFEF4444),
                    iconTint = Color.White,
                    icon = Icons.Default.DeleteOutline
                )
                TvToastType.INFO -> ToastStyle(
                    bg = Color(0xFF0F172A),
                    border = Color(0xFF38BDF8),
                    iconBg = Color(0xFF0284C7),
                    iconTint = Color.White,
                    icon = Icons.Default.Info
                )
            }

            var isFocused by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .shadow(elevation = if (isFocused) 16.dp else 10.dp, shape = RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(bgColor)
                        .border(
                            width = if (isFocused) 2.5.dp else 1.5.dp,
                            color = if (isFocused) Color.White else borderColor,
                            shape = RoundedCornerShape(22.dp)
                        )
                        .onFocusChanged { isFocused = it.isFocused }
                        .focusable()
                        .clickable { onDismiss() }
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                        .testTag("tv_toast_notification")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Large TV Icon container
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(iconBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Title & Message Text for Couch Visibility (10ft away)
                        Column(
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = toast.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = toast.message,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE2E8F0),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Dismiss Pill
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class ToastStyle(
    val bg: Color,
    val border: Color,
    val iconBg: Color,
    val iconTint: Color,
    val icon: ImageVector
)

@Composable
fun ConnectivityWarningBanner(
    warningMessage: String?
) {
    AnimatedVisibility(
        visible = !warningMessage.isNullOrEmpty(),
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it })
    ) {
        if (warningMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(BeamWarningBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("connectivity_warning_banner")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BeamWarning),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "Wi-Fi Warning",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = warningMessage,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78350F),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationBanner(
    message: String?,
    onDismiss: () -> Unit
) {
    if (message.isNullOrEmpty()) return

    val isError = message.contains("error", ignoreCase = true) ||
            message.contains("failed", ignoreCase = true) ||
            message.contains("corrupted", ignoreCase = true) ||
            message.contains("mismatch", ignoreCase = true)

    val isDelete = message.contains("deleted", ignoreCase = true)
    val isTransfer = message.contains("received", ignoreCase = true) || message.contains("transfer", ignoreCase = true) || message.contains("verified", ignoreCase = true)
    val isNetwork = message.contains("wifi", ignoreCase = true) || message.contains("connection", ignoreCase = true) || message.contains("network", ignoreCase = true)

    val toastType = when {
        isError -> TvToastType.ERROR
        isTransfer -> TvToastType.TRANSFER
        isDelete -> TvToastType.DELETE
        isNetwork -> TvToastType.NETWORK
        else -> TvToastType.INFO
    }

    val title = when (toastType) {
        TvToastType.TRANSFER -> "Transfer Complete"
        TvToastType.DELETE -> "File Deleted"
        TvToastType.ERROR -> "Transfer Failed"
        TvToastType.NETWORK -> "Connection Alert"
        else -> "Notification"
    }

    TvToastNotification(
        toast = TvToast(
            title = title,
            message = message,
            type = toastType
        ),
        onDismiss = onDismiss
    )
}
