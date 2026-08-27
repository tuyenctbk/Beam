package com.example.beam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.beam.ui.theme.BeamFocusCyan
import com.example.beam.ui.theme.BeamFocusCyanBright
import com.example.beam.ui.theme.BeamGreen
import com.example.beam.ui.theme.BeamNeonBlue

enum class EmptyStateType {
    EMPTY_FOLDER,
    SEARCH_NO_RESULTS,
    CATEGORY_EMPTY,
    HISTORY_EMPTY,
    STORAGE_EMPTY
}

@Composable
fun BeamEmptyState(
    type: EmptyStateType,
    modifier: Modifier = Modifier,
    customTitle: String? = null,
    customDesc: String? = null,
    searchQuery: String = "",
    onPrimaryAction: (() -> Unit)? = null,
    onSecondaryAction: (() -> Unit)? = null,
    primaryActionLabel: String? = null,
    secondaryActionLabel: String? = null
) {
    val icon: ImageVector = when (type) {
        EmptyStateType.EMPTY_FOLDER -> Icons.Default.FolderOpen
        EmptyStateType.SEARCH_NO_RESULTS -> Icons.Default.SearchOff
        EmptyStateType.CATEGORY_EMPTY -> Icons.Default.FolderOpen
        EmptyStateType.HISTORY_EMPTY -> Icons.Default.History
        EmptyStateType.STORAGE_EMPTY -> Icons.Default.FolderOpen
    }

    val accentColor: Color = when (type) {
        EmptyStateType.SEARCH_NO_RESULTS -> BeamFocusCyanBright
        EmptyStateType.HISTORY_EMPTY -> BeamGreen
        EmptyStateType.CATEGORY_EMPTY -> BeamNeonBlue
        else -> BeamFocusCyanBright
    }

    val title: String = customTitle ?: when (type) {
        EmptyStateType.SEARCH_NO_RESULTS -> stringResource(R.string.empty_state_search_title, searchQuery.trim())
        EmptyStateType.HISTORY_EMPTY -> stringResource(R.string.empty_state_history_title)
        EmptyStateType.CATEGORY_EMPTY -> stringResource(R.string.empty_folder_title)
        EmptyStateType.STORAGE_EMPTY -> stringResource(R.string.empty_folder_title)
        EmptyStateType.EMPTY_FOLDER -> stringResource(R.string.empty_state_file_browser_title)
    }

    val description: String = customDesc ?: when (type) {
        EmptyStateType.SEARCH_NO_RESULTS -> stringResource(R.string.empty_state_search_desc)
        EmptyStateType.HISTORY_EMPTY -> stringResource(R.string.empty_state_history_desc)
        EmptyStateType.CATEGORY_EMPTY -> stringResource(R.string.empty_folder_desc)
        EmptyStateType.STORAGE_EMPTY -> stringResource(R.string.empty_folder_desc)
        EmptyStateType.EMPTY_FOLDER -> stringResource(R.string.empty_state_file_browser_desc)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Hero Illustration Container with Glowing Rings & Dashed Vector Border
        Box(
            modifier = Modifier
                .size(110.dp)
                .drawBehind {
                    // Soft Ambient Glow Behind Illustration
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        radius = size.width * 0.7f
                    )
                    // Dashed Ring Canvas Effect
                    drawCircle(
                        color = accentColor.copy(alpha = 0.35f),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.22f),
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
                    .border(1.5.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Title
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Subtitle / Description
        Text(
            text = description,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(460.dp)
        )

        // Step-by-Step Onboarding Guide Card for Empty Folder / History
        if (type == EmptyStateType.EMPTY_FOLDER || type == EmptyStateType.HISTORY_EMPTY) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .width(480.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.onboarding_tip_title),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.onboarding_step_1),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.onboarding_step_2),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.onboarding_step_3),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Action Buttons Row with TV Focusable Cards
        if (onPrimaryAction != null || onSecondaryAction != null) {
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onSecondaryAction != null) {
                    TvFocusableCard(
                        onClick = onSecondaryAction,
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(20.dp),
                        defaultBgColor = MaterialTheme.colorScheme.surface,
                        focusBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        testTag = "empty_state_secondary_btn"
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = secondaryActionLabel ?: stringResource(R.string.btn_show_all_files),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (onPrimaryAction != null) {
                    TvFocusableCard(
                        onClick = onPrimaryAction,
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(20.dp),
                        defaultBgColor = accentColor.copy(alpha = 0.2f),
                        focusBorderColor = accentColor,
                        testTag = "empty_state_primary_btn"
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (type == EmptyStateType.SEARCH_NO_RESULTS) Icons.Default.Close else Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = primaryActionLabel ?: when (type) {
                                    EmptyStateType.SEARCH_NO_RESULTS -> stringResource(R.string.clear_search)
                                    else -> stringResource(R.string.btn_go_to_web_beam)
                                },
                                fontSize = 13.sp,
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
