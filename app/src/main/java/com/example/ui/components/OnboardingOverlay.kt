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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Tv
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BeamPrimary

data class OnboardingStep(
    val title: String,
    val subtitle: String,
    val instruction: String,
    val icon: ImageVector,
    val badge: String,
    val isPermissionStep: Boolean = false
)

@Composable
fun OnboardingOverlay(
    isVisible: Boolean,
    hasStoragePermission: Boolean = false,
    onRequestPermission: () -> Unit = {},
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    val stepWelcomeTitle = stringResource(R.string.welcome_title)
    val stepWelcomeSub = stringResource(R.string.welcome_sub)
    val stepWelcomeInst = stringResource(R.string.welcome_instruction)

    val stepPermTitle = stringResource(R.string.permission_step_title)
    val stepPermSub = stringResource(R.string.permission_step_sub)
    val stepPermInst = if (hasStoragePermission) {
        stringResource(R.string.permission_granted_instruction)
    } else {
        stringResource(R.string.permission_step_instruction)
    }

    val step1Title = stringResource(R.string.step1_title)
    val step1Sub = stringResource(R.string.step1_sub)
    val step1Inst = stringResource(R.string.step1_instruction)

    val step2Title = stringResource(R.string.step2_title)
    val step2Sub = stringResource(R.string.step2_sub)
    val step2Inst = stringResource(R.string.step2_instruction)

    val steps = remember(
        stepWelcomeTitle, stepPermTitle, stepPermInst, step1Title, step2Title, hasStoragePermission
    ) {
        listOf(
            OnboardingStep(
                title = stepWelcomeTitle,
                subtitle = stepWelcomeSub,
                instruction = stepWelcomeInst,
                icon = Icons.Default.Tv,
                badge = "WELCOME"
            ),
            OnboardingStep(
                title = stepPermTitle,
                subtitle = stepPermSub,
                instruction = stepPermInst,
                icon = if (hasStoragePermission) Icons.Default.CheckCircle else Icons.Default.FolderShared,
                badge = if (hasStoragePermission) "PERMISSION GRANTED" else "STORAGE ACCESS",
                isPermissionStep = true
            ),
            OnboardingStep(
                title = step1Title,
                subtitle = step1Sub,
                instruction = step1Inst,
                icon = Icons.Default.CameraAlt,
                badge = "STEP 1 OF 2"
            ),
            OnboardingStep(
                title = step2Title,
                subtitle = step2Sub,
                instruction = step2Inst,
                icon = Icons.Default.CloudUpload,
                badge = "STEP 2 OF 2"
            )
        )
    }

    var currentStepIndex by remember { mutableStateOf(0) }
    val currentStep = steps[currentStepIndex]

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .padding(32.dp)
                .testTag("onboarding_overlay"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(680.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF0F172A))
                    .border(2.dp, BeamPrimary, RoundedCornerShape(28.dp))
                    .padding(32.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Top Step Indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        steps.indices.forEach { index ->
                            Box(
                                modifier = Modifier
                                    .height(6.dp)
                                    .width(if (index == currentStepIndex) 36.dp else 12.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index == currentStepIndex) BeamPrimary
                                        else Color(0xFF334155)
                                    )
                            )
                        }
                    }

                    // Main Icon Display
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentStep.isPermissionStep && hasStoragePermission)
                                    Color(0xFF10B981).copy(alpha = 0.2f)
                                else BeamPrimary.copy(alpha = 0.15f)
                            )
                            .border(
                                2.dp,
                                if (currentStep.isPermissionStep && hasStoragePermission)
                                    Color(0xFF10B981)
                                else BeamPrimary,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = currentStep.icon,
                            contentDescription = currentStep.title,
                            tint = if (currentStep.isPermissionStep && hasStoragePermission)
                                Color(0xFF34D399)
                            else Color(0xFF38BDF8),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Title & Subtitle
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (currentStep.isPermissionStep && hasStoragePermission)
                                        Color(0xFF059669)
                                    else BeamPrimary
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = currentStep.badge,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = currentStep.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = currentStep.subtitle,
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Instruction Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E293B))
                            .padding(18.dp)
                    ) {
                        Text(
                            text = currentStep.instruction,
                            fontSize = 14.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Buttons Row (TV Remote D-Pad Navigable)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var isSkipFocused by remember { mutableStateOf(false) }

                        // Skip / Dismiss Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSkipFocused) Color(0xFF334155) else Color(0xFF1E293B)
                                )
                                .border(
                                    2.dp,
                                    if (isSkipFocused) BeamPrimary else Color.Transparent,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { onDismiss() }
                                .onFocusChanged { isSkipFocused = it.isFocused }
                                .focusable()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                                .testTag("onboarding_skip_button")
                        ) {
                            Text(
                                text = stringResource(R.string.skip_tutorial),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        var isNextFocused by remember { mutableStateOf(false) }

                        // Action / Next Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    when {
                                        currentStep.isPermissionStep && !hasStoragePermission ->
                                            if (isNextFocused) Color(0xFFD97706) else Color(0xFFF59E0B)
                                        isNextFocused -> Color(0xFF2563EB)
                                        else -> BeamPrimary
                                    }
                                )
                                .border(
                                    3.dp,
                                    if (isNextFocused) Color.White else Color.Transparent,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    if (currentStep.isPermissionStep && !hasStoragePermission) {
                                        onRequestPermission()
                                    } else if (currentStepIndex < steps.size - 1) {
                                        currentStepIndex++
                                    } else {
                                        onDismiss()
                                    }
                                }
                                .onFocusChanged { isNextFocused = it.isFocused }
                                .focusable()
                                .padding(horizontal = 28.dp, vertical = 14.dp)
                                .testTag("onboarding_next_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val buttonText = when {
                                    currentStep.isPermissionStep && !hasStoragePermission ->
                                        stringResource(R.string.permission_grant_btn)
                                    currentStepIndex < steps.size - 1 ->
                                        stringResource(R.string.next_step)
                                    else ->
                                        stringResource(R.string.start_beaming)
                                }

                                Text(
                                    text = buttonText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (currentStepIndex < steps.size - 1 && !(currentStep.isPermissionStep && !hasStoragePermission))
                                        Icons.Default.ArrowForward
                                    else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
