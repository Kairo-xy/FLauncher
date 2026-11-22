package com.verya.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verya.launcher.model.WindowState
import com.verya.launcher.model.WindowType
import com.verya.launcher.theme.CyberColors
import com.verya.launcher.theme.CyberTypography

@Composable
fun QuickDock(
    windows: List<WindowState>,
    activeWindow: WindowState?,
    onToggleWindow: (WindowType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(CyberColors.BgDark)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WindowType.values().forEach { type ->
            val winState = windows.firstOrNull { it.type == type }
            val isOpen = winState?.isVisible?.value == true
            val isFocused = activeWindow?.type == type && isOpen && winState.isMinimized.value.not()

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            isFocused -> CyberColors.Highlight
                            isOpen -> CyberColors.BgCard
                            else -> CyberColors.BgDark
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = when {
                            isFocused -> CyberColors.Cyan
                            isOpen -> CyberColors.GlassBorder
                            else -> CyberColors.BgCard
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onToggleWindow(type) }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = type.defaultIcon,
                        contentDescription = type.title,
                        tint = when {
                            isFocused -> CyberColors.Cyan
                            isOpen -> CyberColors.Blue
                            else -> CyberColors.TextMuted
                        },
                        modifier = Modifier.size(16.dp)
                    )

                    // Small indicator dot if open
                    if (isOpen) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(if (isFocused) CyberColors.Cyan else CyberColors.TextSecondary)
                        )
                    }
                }
            }
        }
    }
}
