package com.verya.launcher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.verya.launcher.model.WindowState
import com.verya.launcher.theme.CyberColors
import com.verya.launcher.theme.CyberTypography
import kotlin.math.roundToInt

@Composable
fun WindowFrame(
    state: WindowState,
    isFocused: Boolean,
    screenWidthDp: Float,
    screenHeightDp: Float,
    onFocus: () -> Unit,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    if (!state.isVisible.value || state.isMinimized.value) return

    val density = LocalDensity.current

    val currentX = if (state.isMaximized.value) 0f else state.x.floatValue
    val currentY = if (state.isMaximized.value) 32f else state.y.floatValue
    val currentW = if (state.isMaximized.value) screenWidthDp else state.width.floatValue
    val currentH = if (state.isMaximized.value) (screenHeightDp - 88f) else state.height.floatValue

    Box(
        modifier = Modifier
            .offset { IntOffset(currentX.dp.roundToPx(), currentY.dp.roundToPx()) }
            .size(currentW.dp, currentH.dp)
            .zIndex(state.zIndex.floatValue)
            .shadow(
                elevation = if (isFocused) 16.dp else 8.dp,
                shape = RoundedCornerShape(10.dp)
            )
            .clip(RoundedCornerShape(10.dp))
            .background(CyberColors.GlassSurface)
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                color = if (isFocused) CyberColors.GlassBorderFocused else CyberColors.GlassBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onFocus() }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Window Header / Titlebar (Draggable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(CyberColors.GlassHeader)
                    .pointerInput(state.isMaximized.value) {
                        if (!state.isMaximized.value) {
                            detectDragGestures(
                                onDragStart = { onFocus() }
                            ) { change, dragAmount ->
                                change.consume()
                                val newX = (state.x.floatValue + dragAmount.x / density.density)
                                    .coerceIn(0f, (screenWidthDp - 80f).coerceAtLeast(0f))
                                val newY = (state.y.floatValue + dragAmount.y / density.density)
                                    .coerceIn(24f, (screenHeightDp - 80f).coerceAtLeast(24f))
                                state.x.floatValue = newX
                                state.y.floatValue = newY
                            }
                        }
                    }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Title & Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = state.type.defaultIcon,
                        contentDescription = null,
                        tint = if (isFocused) CyberColors.Cyan else CyberColors.TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = state.type.title,
                        color = if (isFocused) CyberColors.TextPrimary else CyberColors.TextSecondary,
                        fontFamily = CyberTypography.Mono,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }

                // Window Action Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Minimize
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0x33E0AF68))
                            .clickable {
                                state.isMinimized.value = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Minimize",
                            tint = CyberColors.Yellow,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    // Maximize / Restore
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0x3373DACA))
                            .clickable {
                                if (state.isMaximized.value) {
                                    state.isMaximized.value = false
                                    state.x.floatValue = state.prevX
                                    state.y.floatValue = state.prevY
                                    state.width.floatValue = state.prevW
                                    state.height.floatValue = state.prevH
                                } else {
                                    state.prevX = state.x.floatValue
                                    state.prevY = state.y.floatValue
                                    state.prevW = state.width.floatValue
                                    state.prevH = state.height.floatValue
                                    state.isMaximized.value = true
                                }
                                onFocus()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CropSquare,
                            contentDescription = "Maximize",
                            tint = CyberColors.Cyan,
                            modifier = Modifier.size(11.dp)
                        )
                    }

                    // Close
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0x33F7768E))
                            .clickable {
                                onClose()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CyberColors.Pink,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Window Body Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                content()
            }
        }

        // Bottom-Right Corner Resize Handle (when not maximized)
        if (!state.isMaximized.value) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(24.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { onFocus() }
                        ) { change, dragAmount ->
                            change.consume()
                            val newW = (state.width.floatValue + dragAmount.x / density.density)
                                .coerceIn(240f, screenWidthDp)
                            val newH = (state.height.floatValue + dragAmount.y / density.density)
                                .coerceIn(180f, (screenHeightDp - 60f))
                            state.width.floatValue = newW
                            state.height.floatValue = newH
                        }
                    }
                    .padding(4.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = "⌟",
                    color = if (isFocused) CyberColors.Cyan else CyberColors.TextMuted,
                    fontSize = 16.sp,
                    fontFamily = CyberTypography.Mono,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
