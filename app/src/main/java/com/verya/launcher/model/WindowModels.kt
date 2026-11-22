package com.verya.launcher.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

enum class WindowType(val title: String) {
    TERMINAL("Terminal"),
    BROWSER("Cyber Browser"),
    WEATHER("Atmosphere"),
    MAPS("Cyber Map"),
    NOTES("Quick Notes"),
    FILES("Storage Files"),
    APPS("App Drawer"),
    SETTINGS("System & Settings");

    val defaultIcon: ImageVector
        get() = when (this) {
            TERMINAL -> Icons.Default.Terminal
            BROWSER -> Icons.Default.Language
            WEATHER -> Icons.Default.WbSunny
            MAPS -> Icons.Default.Map
            NOTES -> Icons.Default.Description
            FILES -> Icons.Default.Folder
            APPS -> Icons.Default.Apps
            SETTINGS -> Icons.Default.Settings
        }
}

class WindowState(
    val type: WindowType,
    initX: Float,
    initY: Float,
    initWidth: Float,
    initHeight: Float,
    initZ: Float = 1f,
    initVisible: Boolean = false
) {
    var x = androidx.compose.runtime.mutableFloatStateOf(initX)
    var y = androidx.compose.runtime.mutableFloatStateOf(initY)
    var width = androidx.compose.runtime.mutableFloatStateOf(initWidth)
    var height = androidx.compose.runtime.mutableFloatStateOf(initHeight)
    var zIndex = androidx.compose.runtime.mutableFloatStateOf(initZ)
    var isMinimized = androidx.compose.runtime.mutableStateOf(false)
    var isMaximized = androidx.compose.runtime.mutableStateOf(false)
    var isVisible = androidx.compose.runtime.mutableStateOf(initVisible)

    // Stored dimensions for restore after maximize
    var prevX = initX
    var prevY = initY
    var prevW = initWidth
    var prevH = initHeight
}
