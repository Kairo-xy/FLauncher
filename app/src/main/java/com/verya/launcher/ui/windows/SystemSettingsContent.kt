package com.verya.launcher.ui.windows

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verya.launcher.theme.CyberColors
import com.verya.launcher.theme.CyberTypography

enum class WallpaperTheme(val label: String) {
    NEON_GRID("Neon Cyber Grid"),
    DEEP_MATRIX("Deep Matrix"),
    DARK_AURORA("Dark Aurora"),
    CHARCOAL_GLASS("Charcoal Glass"),
    SYSTEM_WALLPAPER("Live System Wallpaper")
}

@Composable
fun SystemSettingsContent(
    currentTheme: WallpaperTheme,
    onSelectTheme: (WallpaperTheme) -> Unit
) {
    val context = LocalContext.current
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val memInfo = ActivityManager.MemoryInfo().apply { am?.getMemoryInfo(this) }

    val totalRamMb = memInfo.totalMem / (1024 * 1024)
    val availRamMb = memInfo.availMem / (1024 * 1024)
    val usedRamMb = totalRamMb - availRamMb
    val ramPercentage = if (totalRamMb > 0) ((usedRamMb.toDouble() / totalRamMb) * 100).toInt() else 0

    val uptimeHours = SystemClock.uptimeMillis() / (1000 * 60 * 60)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberColors.BgDark)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section: System Specifications
        item {
            Text(
                text = "SYSTEM SPECIFICATIONS",
                color = CyberColors.TerminalPrompt,
                fontFamily = CyberTypography.Mono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyberColors.BgCard)
                    .border(1.dp, CyberColors.GlassBorder, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SpecRow("DEVICE", "${Build.MANUFACTURER.uppercase()} ${Build.MODEL}")
                SpecRow("ANDROID OS", "v${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                SpecRow("MEMORY", "$usedRamMb MB / $totalRamMb MB ($ramPercentage% used)")
                SpecRow("UPTIME", "$uptimeHours hours")
            }
        }

        // Section: Wallpaper & Workspace Theme
        item {
            Text(
                text = "DESKTOP WALLPAPER THEME",
                color = CyberColors.TerminalPrompt,
                fontFamily = CyberTypography.Mono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                WallpaperTheme.values().forEach { theme ->
                    val isSelected = theme == currentTheme
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) CyberColors.Highlight else CyberColors.BgCard)
                            .border(
                                1.dp,
                                if (isSelected) CyberColors.Cyan else CyberColors.GlassBorder,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { onSelectTheme(theme) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = theme.label,
                            color = if (isSelected) CyberColors.Cyan else CyberColors.TextPrimary,
                            fontFamily = CyberTypography.Mono,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Text(
                                text = "ACTIVE",
                                color = CyberColors.Green,
                                fontFamily = CyberTypography.Mono,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Section: System Shortcuts
        item {
            Text(
                text = "SYSTEM CONFIGURATION",
                color = CyberColors.TerminalPrompt,
                fontFamily = CyberTypography.Mono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ActionRow(
                    icon = Icons.Default.Home,
                    label = "Set as Default Home Launcher",
                    onClick = {
                        try {
                            context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        } catch (_: Exception) {
                            context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        }
                    }
                )

                ActionRow(
                    icon = Icons.Default.Settings,
                    label = "Open Android System Settings",
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    }
                )
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = CyberColors.TextMuted,
            fontFamily = CyberTypography.Mono,
            fontSize = 11.sp
        )
        Text(
            text = value,
            color = CyberColors.TextPrimary,
            fontFamily = CyberTypography.Mono,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(CyberColors.BgCard)
            .border(1.dp, CyberColors.GlassBorder, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CyberColors.Cyan,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            color = CyberColors.TextPrimary,
            fontFamily = CyberTypography.Mono,
            fontSize = 12.sp
        )
    }
}
