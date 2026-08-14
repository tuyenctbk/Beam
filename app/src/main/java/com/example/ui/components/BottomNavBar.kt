package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.theme.BeamOnPrimaryContainer
import com.example.ui.theme.BeamPrimaryContainer
import com.example.ui.theme.BeamSecondary

enum class NavTab(@StringRes val titleResId: Int, val fallbackTitle: String, val icon: ImageVector) {
    BEAM(R.string.nav_beam, "BEAM", Icons.Default.QrCodeScanner),
    FILES(R.string.nav_files, "FILES", Icons.Default.Folder),
    APKS(R.string.nav_apks, "APKS", Icons.Default.Android),
    REMOTE(R.string.nav_remote, "REMOTE", Icons.Default.Keyboard),
    STORAGE(R.string.nav_storage, "STORAGE", Icons.Default.Storage),
    HISTORY(R.string.nav_history, "HISTORY", Icons.Default.History),
    HELP(R.string.nav_help, "HELP", Icons.Default.HelpOutline),
    SETTINGS(R.string.nav_settings, "SETTINGS", Icons.Default.Settings)
}

@Composable
fun BottomNavBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .testTag("bottom_nav_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                var isFocused by remember { mutableStateOf(false) }
                val tabTitle = stringResource(tab.titleResId)

                val scale by animateFloatAsState(
                    targetValue = if (isFocused) 1.12f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 350f),
                    label = "tabScale"
                )

                val pillBg by animateColorAsState(
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else if (isFocused) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent,
                    label = "tabPillBg"
                )
                val textColor by animateColorAsState(
                    if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    label = "tabTextColor"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isFocused) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                        .border(
                            width = if (isFocused) 2.dp else 0.dp,
                            color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .onFocusChanged { isFocused = it.isFocused }
                        .focusable()
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("nav_tab_${tab.name.lowercase()}")
                ) {
                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(pillBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tabTitle,
                            tint = textColor,
                            modifier = Modifier.width(20.dp).height(20.dp)
                        )
                    }

                    Text(
                        text = tabTitle,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                        color = textColor,
                        letterSpacing = 0.6.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
