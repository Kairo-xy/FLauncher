package com.verya.launcher.ui

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verya.launcher.theme.CyberColors
import com.verya.launcher.theme.CyberTypography
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatusBar(
    activeContextName: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val systemInfo by produceState(
        initialValue = SystemStatus(battery = 100, isCharging = false, uptime = "0m", ramUsage = "0%", time = "")
    ) {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val timeFmt = SimpleDateFormat("EEE d MMM  HH:mm", Locale.getDefault())

        while (true) {
            val bat = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
            val batStatus = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) ?: -1
            val isCharging = batStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                    batStatus == BatteryManager.BATTERY_STATUS_FULL

            val uptimeMs = SystemClock.uptimeMillis()
            val hours = (uptimeMs / (1000 * 60 * 60)).toInt()
            val minutes = ((uptimeMs / (1000 * 60)) % 60).toInt()
            val upStr = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

            val memInfo = ActivityManager.MemoryInfo()
            am?.getMemoryInfo(memInfo)
            val ramPct = if (memInfo.totalMem > 0) {
                val used = memInfo.totalMem - memInfo.availMem
                "${((used.toDouble() / memInfo.totalMem) * 100).toInt()}%"
            } else "N/A"

            value = SystemStatus(
                battery = bat,
                isCharging = isCharging,
                uptime = upStr,
                ramUsage = ramPct,
                time = timeFmt.format(Date())
            )
            delay(1000)
        }
    }

    val shellPrompt = if (activeContextName != null) {
        "root@flauncher:~/${activeContextName.lowercase()}"
    } else {
        "root@flauncher:~"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(CyberColors.BgDark)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Terminal shell prompt
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = shellPrompt,
                color = CyberColors.TerminalPrompt,
                fontFamily = CyberTypography.Mono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "·",
                color = CyberColors.TextMuted,
                fontFamily = CyberTypography.Mono,
                fontSize = 11.sp
            )
            Text(
                text = "up ${systemInfo.uptime}",
                color = CyberColors.TextMuted,
                fontFamily = CyberTypography.Mono,
                fontSize = 11.sp
            )
        }

        // Right: System specs & Clock
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "RAM ${systemInfo.ramUsage}",
                color = CyberColors.Cyan,
                fontFamily = CyberTypography.Mono,
                fontSize = 11.sp
            )
            Text(
                text = "BAT ${systemInfo.battery}%${if (systemInfo.isCharging) "⚡" else ""}",
                color = if (systemInfo.battery <= 20) CyberColors.Pink else CyberColors.Green,
                fontFamily = CyberTypography.Mono,
                fontSize = 11.sp
            )
            Text(
                text = systemInfo.time,
                color = CyberColors.TextSecondary,
                fontFamily = CyberTypography.Mono,
                fontSize = 11.sp
            )
        }
    }
}

private data class SystemStatus(
    val battery: Int,
    val isCharging: Boolean,
    val uptime: String,
    val ramUsage: String,
    val time: String
)
