package com.example.beam.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.beam.ui.theme.BeamBorder
import com.example.beam.ui.theme.BeamBorderLight
import com.example.beam.ui.theme.BeamCardBg
import com.example.beam.ui.theme.BeamCardBgFocused
import com.example.beam.ui.theme.BeamFocusCyan
import com.example.beam.ui.theme.BeamFocusGlowSoft

@Composable
fun TvFocusableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    focusBorderColor: Color = BeamFocusCyan,
    defaultBorderColor: Color = BeamBorder,
    defaultBgColor: Color = BeamCardBg,
    focusedBgColor: Color = BeamCardBgFocused,
    elevation: Dp = 4.dp,
    focusedScale: Float = 1.04f,
    testTag: String = "tv_focusable_card",
    content: @Composable BoxScope.(isFocused: Boolean) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.97f
            isFocused -> focusedScale
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tv_card_scale"
    )

    val currentBorderColor by animateColorAsState(
        targetValue = when {
            isPressed || isFocused -> focusBorderColor
            else -> defaultBorderColor
        },
        animationSpec = tween(durationMillis = 150),
        label = "tv_border_color"
    )

    val currentBgColor by animateColorAsState(
        targetValue = when {
            isPressed -> focusBorderColor.copy(alpha = 0.28f)
            isFocused -> focusedBgColor
            else -> defaultBgColor
        },
        animationSpec = tween(durationMillis = 150),
        label = "tv_bg_color"
    )

    val currentBorderWidth by animateDpAsState(
        targetValue = if (isPressed || isFocused) 2.5.dp else 1.dp,
        animationSpec = tween(durationMillis = 150),
        label = "tv_border_width"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .testTag(testTag)
            .onFocusChanged { isFocused = it.isFocused }
            .scale(scale)
            .focusable(),
        interactionSource = interactionSource,
        shape = shape,
        color = currentBgColor,
        border = BorderStroke(
            width = currentBorderWidth,
            color = currentBorderColor
        ),
        shadowElevation = if (isFocused || isPressed) elevation * 2.5f else elevation
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle neon gradient shimmer overlay when focused or pressed
            if (isFocused || isPressed) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    focusBorderColor.copy(alpha = if (isPressed) 0.25f else 0.12f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
            content(isFocused || isPressed)
        }
    }
}
