package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BeamPrimary

@Composable
fun RateAppDialog(
    isVisible: Boolean,
    onRateSubmitted: (Int) -> Unit,
    onDismiss: () -> Unit,
    onDontShowAgain: () -> Unit
) {
    if (!isVisible) return

    var selectedStars by remember { mutableStateOf(5) }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .padding(32.dp)
                .testTag("rate_app_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(580.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0F172A))
                    .border(2.dp, BeamPrimary, RoundedCornerShape(24.dp))
                    .padding(28.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(BeamPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = stringResource(R.string.rate_beam),
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = stringResource(R.string.rate_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(R.string.rate_sub),
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )

                    // 5 Stars Row (TV D-Pad Navigable)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        (1..5).forEach { starIndex ->
                            var isStarFocused by remember { mutableStateOf(false) }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isStarFocused) Color(0xFF334155)
                                        else if (starIndex <= selectedStars) Color(0xFFF59E0B).copy(alpha = 0.2f)
                                        else Color(0xFF1E293B)
                                    )
                                    .border(
                                        2.dp,
                                        if (isStarFocused) Color(0xFFF59E0B) else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { selectedStars = starIndex }
                                    .onFocusChanged { isStarFocused = it.isFocused }
                                    .focusable(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (starIndex <= selectedStars) Icons.Default.Star else Icons.Default.StarOutline,
                                    contentDescription = "Star $starIndex",
                                    tint = if (starIndex <= selectedStars) Color(0xFFF59E0B) else Color(0xFF64748B),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var isSubmitFocused by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSubmitFocused) Color(0xFF2563EB) else BeamPrimary)
                                .border(2.dp, if (isSubmitFocused) Color.White else Color.Transparent, RoundedCornerShape(14.dp))
                                .clickable {
                                    onRateSubmitted(selectedStars)
                                }
                                .onFocusChanged { isSubmitFocused = it.isFocused }
                                .focusable()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.submit_stars, selectedStars),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        var isDismissFocused by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDismissFocused) Color(0xFF334155) else Color(0xFF1E293B))
                                .border(2.dp, if (isDismissFocused) BeamPrimary else Color.Transparent, RoundedCornerShape(14.dp))
                                .clickable { onDismiss() }
                                .onFocusChanged { isDismissFocused = it.isFocused }
                                .focusable()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.later),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        var isNeverFocused by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isNeverFocused) Color(0xFF334155) else Color.Transparent)
                                .border(1.dp, if (isNeverFocused) Color.White else Color(0xFF475569), RoundedCornerShape(14.dp))
                                .clickable { onDontShowAgain() }
                                .onFocusChanged { isNeverFocused = it.isFocused }
                                .focusable()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_thanks),
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}
