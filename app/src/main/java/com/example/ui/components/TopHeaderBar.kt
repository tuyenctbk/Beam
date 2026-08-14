package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.theme.BeamOnPrimaryContainer
import com.example.ui.theme.BeamPrimary
import com.example.ui.theme.BeamPrimaryContainer

@Composable
fun TopHeaderBar(
    isServerRunning: Boolean,
    wifiSsid: String,
    onRefresh: () -> Unit,
    onPowerSaverToggle: () -> Unit = {},
    onInfoClick: () -> Unit
) {
    val onlineText = stringResource(R.string.online)
    val offlineText = stringResource(R.string.offline)
    val appTitle = stringResource(R.string.app_name)
    val refreshDesc = stringResource(R.string.refresh)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo & Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BeamPrimary),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
            Text(
                text = appTitle,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = BeamOnPrimaryContainer,
                letterSpacing = (-0.5).sp
            )

            // Status chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isServerRunning) BeamPrimaryContainer else Color(0xFFFFE0E0))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isServerRunning) "$onlineText • $wifiSsid" else offlineText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isServerRunning) BeamOnPrimaryContainer else Color(0xFFD32F2F)
                )
            }
        }

        // Action Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Power Saver Toggle Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCFCE7))
                    .clickable { onPowerSaverToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EnergySavingsLeaf,
                    contentDescription = "Toggle Power Saver",
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEFF1F5))
                    .clickable { onRefresh() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = refreshDesc,
                    tint = BeamOnPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEFF1F5))
                    .clickable { onInfoClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "About Beam",
                    tint = BeamOnPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
