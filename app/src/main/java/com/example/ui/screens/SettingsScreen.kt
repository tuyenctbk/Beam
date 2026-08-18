package com.example.ui.screens

import com.example.R
import androidx.compose.ui.res.stringResource

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TvRemoteShortcutsBar
import com.example.ui.theme.BeamPrimary

import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Share

@Composable
fun SettingsScreen(
    isHighContrastDark: Boolean,
    isPowerSaverActive: Boolean,
    isAutoCleanOldFilesEnabled: Boolean,
    serverPort: Int,
    downloadDirName: String,
    onToggleHighContrastDark: () -> Unit,
    onTogglePowerSaver: () -> Unit,
    onToggleAutoCleanOldFiles: () -> Unit,
    onCleanOldFilesNow: () -> Unit,
    onReplayOnboarding: () -> Unit,
    onTriggerShareDialog: () -> Unit,
    onTriggerRateDialog: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Settings Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(BeamPrimary)
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_pref_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.settings_pref_desc),
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // Setting Item 1: High Contrast Dark Mode
        item {
            SettingSwitchCard(
                title = "High-Contrast Dark Mode",
                subtitle = "Reduces TV screen glare in dark rooms with high contrast dark background.",
                icon = Icons.Default.DarkMode,
                isChecked = isHighContrastDark,
                onCheckedChange = { onToggleHighContrastDark() },
                testTag = "setting_dark_mode_toggle"
            )
        }

        // Setting Item 2: Auto-Clean Files Older Than 30 Days
        item {
            SettingSwitchCard(
                title = "Auto-Clean Old Downloads (>30 Days)",
                subtitle = "Automatically deletes files older than 30 days in the Downloads folder on app launch.",
                icon = Icons.Default.CleaningServices,
                isChecked = isAutoCleanOldFilesEnabled,
                onCheckedChange = { onToggleAutoCleanOldFiles() },
                testTag = "setting_auto_clean_toggle"
            )
        }

        // Setting Action Card: Clean Old Files Now
        item {
            SettingActionCard(
                title = "Clean Up Old Files Now (>30 Days)",
                subtitle = "Immediately scan and delete files in Downloads folder older than 30 days to free up TV storage.",
                icon = Icons.Default.CleaningServices,
                buttonText = "Clean Now",
                onClick = onCleanOldFilesNow
            )
        }

        // Setting Action Card: Replay Quick Start Onboarding
        item {
            SettingActionCard(
                title = "Replay Quick Start Tutorial",
                subtitle = "Show the camera scanning and file transfer onboarding screens again.",
                icon = Icons.Default.HelpOutline,
                buttonText = "Replay Tutorial",
                onClick = onReplayOnboarding
            )
        }

        // Setting Item: Power Saver Mode
        item {
            SettingSwitchCard(
                title = "TV Power Saver Mode",
                subtitle = "Automatically dims screen after 5 minutes of idle time. Press any button to wake up.",
                icon = Icons.Default.EnergySavingsLeaf,
                isChecked = isPowerSaverActive,
                onCheckedChange = { onTogglePowerSaver() },
                testTag = "setting_power_saver_toggle"
            )
        }

        // Setting Action Card: Share App & Rate App
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SettingActionCard(
                        title = "Share Beam TV",
                        subtitle = "Show QR code / link to family.",
                        icon = Icons.Default.Share,
                        buttonText = "Share App",
                        onClick = onTriggerShareDialog
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    SettingActionCard(
                        title = "Rate Beam TV",
                        subtitle = "Leave a 5-star rating.",
                        icon = Icons.Default.RateReview,
                        buttonText = "Rate App",
                        onClick = onTriggerRateDialog
                    )
                }
            }
        }

        // Setting Item 3: Web Portal Server Info
        item {
            SettingInfoCard(
                title = "Web Server Port",
                subtitle = "Current active port: $serverPort. Connect browsers via http://<TV_IP>:$serverPort",
                icon = Icons.Default.Language,
                badgeText = "PORT $serverPort"
            )
        }

        // Setting Item 4: Default Storage Directory
        item {
            SettingInfoCard(
                title = "Default Downloads Directory",
                subtitle = "Received files from browser portal are stored in $downloadDirName.",
                icon = Icons.Default.Storage,
                badgeText = "STORAGE"
            )
        }

        item {
            TvRemoteShortcutsBar()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    buttonText: String,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val containerBg = if (isFocused) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(containerBg)
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 18.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(BeamPrimary)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun SettingSwitchCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val containerBg = if (isFocused) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(containerBg)
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onCheckedChange(!isChecked) }
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .padding(20.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 18.sp
                )
            }

            Switch(
                checked = isChecked,
                onCheckedChange = { onCheckedChange(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = BeamPrimary
                )
            )
        }
    }
}

@Composable
private fun SettingInfoCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeText: String
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val containerBg = if (isFocused) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(containerBg)
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 18.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(BeamPrimary)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
