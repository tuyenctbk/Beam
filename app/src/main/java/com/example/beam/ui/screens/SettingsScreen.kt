package com.example.beam.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.beam.ui.components.TvFocusableCard
import com.example.beam.ui.theme.BeamAmber
import com.example.beam.ui.theme.BeamBorder
import com.example.beam.ui.theme.BeamBorderLight
import com.example.beam.ui.theme.BeamFocusCyan
import com.example.beam.ui.theme.BeamFocusCyanBright
import com.example.beam.ui.theme.BeamGreen
import com.example.beam.ui.theme.BeamNeonBlue
import com.example.beam.ui.theme.BeamPurple
import com.example.beam.ui.theme.BeamRose
import com.example.beam.ui.theme.ThemeMode
import com.example.beam.ui.viewmodel.MainViewModel

import androidx.compose.foundation.layout.BoxWithConstraints

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isServerRunning by viewModel.isServerRunning.collectAsState()
    val serverPort by viewModel.serverPort.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val autoAddToHistory by viewModel.autoAddToHistory.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val batteryState by viewModel.batteryState.collectAsState()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isCompact = maxWidth < 600.dp
        val horizontalPadding = if (isCompact) 16.dp else 24.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
        ) {
        // App Info Banner Card
        item {
            TvFocusableCard(
                onClick = { viewModel.showToast("Beam TV - Version 1.0.1 (Build 2026)") },
                modifier = Modifier.fillMaxWidth(),
                testTag = "settings_app_info_card"
            ) { isFocused ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(BeamFocusCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = BeamFocusCyanBright,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.app_name),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.app_tagline),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BeamFocusCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.version_label, "1.0.1"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BeamFocusCyanBright
                        )
                    }
                }
            }
        }

        // Setting 1: System-wide Theme Selection (Dark / Light / Dynamic)
        item {
            TvFocusableCard(
                onClick = {
                    val nextMode = when (themeMode) {
                        ThemeMode.DARK -> ThemeMode.LIGHT
                        ThemeMode.LIGHT -> ThemeMode.DYNAMIC
                        ThemeMode.DYNAMIC -> ThemeMode.DARK
                    }
                    viewModel.setThemeMode(nextMode)
                },
                modifier = Modifier.fillMaxWidth(),
                focusBorderColor = BeamPurple,
                testTag = "settings_theme_toggle_card"
            ) { isFocused ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BeamPurple.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (themeMode) {
                                        ThemeMode.DARK -> Icons.Default.DarkMode
                                        ThemeMode.LIGHT -> Icons.Default.BrightnessHigh
                                        ThemeMode.DYNAMIC -> Icons.Default.AutoAwesome
                                    },
                                    contentDescription = null,
                                    tint = BeamPurple,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.setting_theme_title),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.setting_theme_desc),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Theme Mode Selector Badges
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ThemeMode.values().forEach { mode ->
                                val isSelected = themeMode == mode
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) BeamPurple.copy(alpha = 0.3f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) BeamPurple else MaterialTheme.colorScheme.outlineVariant,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = when (mode) {
                                            ThemeMode.DARK -> stringResource(R.string.theme_dark)
                                            ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                            ThemeMode.DYNAMIC -> stringResource(R.string.theme_dynamic)
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) BeamPurple else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Setting: Low Power & Battery Protection Safety Card
        item {
            TvFocusableCard(
                onClick = {
                    viewModel.toggleSimulateLowBattery(!batteryState.isSimulatedLowBattery)
                },
                modifier = Modifier.fillMaxWidth(),
                focusBorderColor = BeamRose,
                testTag = "settings_battery_safety_card"
            ) { isFocused ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BeamRose.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatteryAlert,
                                contentDescription = null,
                                tint = BeamRose,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.setting_battery_title),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (batteryState.isLowBattery) BeamRose.copy(alpha = 0.2f)
                                            else BeamGreen.copy(alpha = 0.2f)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (batteryState.isCharging) stringResource(R.string.status_charging, batteryState.displayPercent)
                                               else if (batteryState.isLowBattery) stringResource(R.string.status_low_battery, batteryState.displayPercent)
                                               else stringResource(R.string.status_battery_ok, batteryState.displayPercent),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (batteryState.isLowBattery) BeamRose else BeamGreen
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.simulate_low_battery_desc),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = if (batteryState.isSimulatedLowBattery) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                        contentDescription = null,
                        tint = if (batteryState.isSimulatedLowBattery) BeamRose else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Setting 2: Auto-Add Transferred Files to History Setting
        item {
            TvFocusableCard(
                onClick = {
                    viewModel.setAutoAddToHistory(!autoAddToHistory)
                },
                modifier = Modifier.fillMaxWidth(),
                focusBorderColor = BeamGreen,
                testTag = "settings_auto_history_card"
            ) { isFocused ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BeamGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = BeamGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.setting_auto_history_title),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.setting_auto_history_desc),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Interactive Switch Indicator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (autoAddToHistory) BeamGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                1.dp,
                                if (autoAddToHistory) BeamGreen else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (autoAddToHistory) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                                contentDescription = null,
                                tint = if (autoAddToHistory) BeamGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (autoAddToHistory) stringResource(R.string.status_enabled) else stringResource(R.string.status_disabled),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (autoAddToHistory) BeamGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Setting 3: Transfer Server Control Card
        item {
            TvFocusableCard(
                onClick = {
                    if (isServerRunning) {
                        viewModel.stopBeamServer()
                        viewModel.showToast("Server stopped")
                    } else {
                        viewModel.startBeamServer()
                        viewModel.showToast("Server started")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "settings_server_toggle_card"
            ) { isFocused ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BeamNeonBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Router,
                                contentDescription = null,
                                tint = BeamFocusCyanBright,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "${stringResource(R.string.setting_server_title)} (Port $serverPort)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isServerRunning) stringResource(R.string.server_active_at, serverUrl) else stringResource(R.string.server_stopped_click),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isServerRunning) BeamGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                1.dp,
                                if (isServerRunning) BeamGreen else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isServerRunning) stringResource(R.string.status_running) else stringResource(R.string.status_stopped),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isServerRunning) BeamGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Setting 4: Clean Cache Button
        item {
            TvFocusableCard(
                onClick = { viewModel.cleanCache() },
                modifier = Modifier.fillMaxWidth(),
                focusBorderColor = BeamAmber,
                testTag = "settings_clean_cache_card"
            ) { isFocused ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BeamAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            tint = BeamAmber,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.setting_cache_title),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.setting_cache_desc),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Setting 5: Privacy & Local Network Badge
        item {
            TvFocusableCard(
                onClick = { viewModel.showToast("All transfers are direct peer-to-peer over LAN") },
                modifier = Modifier.fillMaxWidth(),
                testTag = "settings_privacy_badge_card"
            ) { isFocused ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BeamFocusCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = BeamFocusCyanBright,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.setting_privacy_title),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.setting_privacy_desc),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
}
