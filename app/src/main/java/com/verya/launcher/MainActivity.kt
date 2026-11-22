package com.verya.launcher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.app.NotificationManagerCompat
import com.verya.launcher.data.AppsManager
import com.verya.launcher.data.NotesManager
import com.verya.launcher.data.NotificationStore
import com.verya.launcher.model.AppItem
import com.verya.launcher.model.TermLine
import com.verya.launcher.model.WindowState
import com.verya.launcher.model.WindowType
import com.verya.launcher.theme.CyberColors
import com.verya.launcher.ui.CommandBar
import com.verya.launcher.ui.QuickDock
import com.verya.launcher.ui.StatusBar
import com.verya.launcher.ui.WindowFrame
import com.verya.launcher.ui.windows.AppsDrawerContent
import com.verya.launcher.ui.windows.BrowserContent
import com.verya.launcher.ui.windows.FilesContent
import com.verya.launcher.ui.windows.MapContent
import com.verya.launcher.ui.windows.NotesContent
import com.verya.launcher.ui.windows.SystemSettingsContent
import com.verya.launcher.ui.windows.TerminalContent
import com.verya.launcher.ui.windows.WallpaperTheme
import com.verya.launcher.ui.windows.WeatherContent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    // Bumped every time the launcher comes back to the front so the app list
    // picks up installs and uninstalls.
    private var resumeTick by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DesktopWorkspace(context = this, refreshKey = resumeTick)
        }
    }

    override fun onResume() {
        super.onResume()
        resumeTick++
    }
}

private fun normalizeUrl(input: String): String {
    val t = input.trim()
    return when {
        t.isEmpty() -> "https://duckduckgo.com"
        t.startsWith("http://") || t.startsWith("https://") -> t
        t.contains(".") && !t.contains(" ") -> "https://$t"
        else -> "https://duckduckgo.com/?q=" + Uri.encode(t)
    }
}

@Composable
fun DesktopWorkspace(context: Context, refreshKey: Int = 0) {
    val appsManager = remember { AppsManager(context) }
    val notesManager = remember { NotesManager(context) }

    var installedApps by remember { mutableStateOf(emptyList<AppItem>()) }
    val terminalLines = remember { mutableStateListOf<TermLine>() }
    var commandInput by remember { mutableStateOf("") }
    var wallpaperTheme by remember { mutableStateOf(WallpaperTheme.NEON_GRID) }

    // Browser navigation requests coming from terminal commands
    var browserUrl by remember { mutableStateOf<String?>(null) }
    var browserNonce by remember { mutableIntStateOf(0) }

    // Floating window state definitions
    val terminalWin = remember {
        WindowState(WindowType.TERMINAL, initX = 14f, initY = 38f, initWidth = 320f, initHeight = 240f, initZ = 2f, initVisible = true)
    }
    val browserWin = remember {
        WindowState(WindowType.BROWSER, initX = 24f, initY = 50f, initWidth = 330f, initHeight = 360f, initZ = 1f, initVisible = false)
    }
    val weatherWin = remember {
        WindowState(WindowType.WEATHER, initX = 30f, initY = 60f, initWidth = 310f, initHeight = 320f, initZ = 1f, initVisible = false)
    }
    val mapsWin = remember {
        WindowState(WindowType.MAPS, initX = 20f, initY = 45f, initWidth = 320f, initHeight = 340f, initZ = 1f, initVisible = false)
    }
    val notesWin = remember {
        WindowState(WindowType.NOTES, initX = 25f, initY = 55f, initWidth = 310f, initHeight = 300f, initZ = 1f, initVisible = false)
    }
    val filesWin = remember {
        WindowState(WindowType.FILES, initX = 20f, initY = 50f, initWidth = 310f, initHeight = 290f, initZ = 1f, initVisible = false)
    }
    val appsWin = remember {
        WindowState(WindowType.APPS, initX = 16f, initY = 42f, initWidth = 330f, initHeight = 340f, initZ = 1f, initVisible = false)
    }
    val settingsWin = remember {
        WindowState(WindowType.SETTINGS, initX = 22f, initY = 48f, initWidth = 320f, initHeight = 320f, initZ = 1f, initVisible = false)
    }

    val windows = remember {
        listOf(terminalWin, browserWin, weatherWin, mapsWin, notesWin, filesWin, appsWin, settingsWin)
    }

    var topZIndex by remember { mutableFloatStateOf(3f) }
    var focusedWindow by remember { mutableStateOf<WindowState?>(terminalWin) }

    fun bringToFront(win: WindowState) {
        topZIndex += 1f
        win.zIndex.floatValue = topZIndex
        win.isMinimized.value = false
        win.isVisible.value = true
        focusedWindow = win
    }

    fun toggleWindow(type: WindowType) {
        val win = windows.first { it.type == type }
        if (win.isVisible.value && !win.isMinimized.value && focusedWindow == win) {
            // Already active and on top -> minimize
            win.isMinimized.value = true
            focusedWindow = windows.filter { it.isVisible.value && !it.isMinimized.value }
                .maxByOrNull { it.zIndex.floatValue }
        } else {
            bringToFront(win)
        }
    }

    fun out(text: String, kind: Int = 0) {
        terminalLines.add(TermLine(text, kind))
    }

    fun findApp(query: String): AppItem? {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return null
        return installedApps.firstOrNull { it.label.lowercase() == q }
            ?: installedApps.firstOrNull { it.label.lowercase().startsWith(q) }
            ?: installedApps.firstOrNull { it.label.lowercase().contains(q) }
    }

    fun startSafe(intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: Exception) {
        false
    }

    fun openBrowser(url: String?) {
        if (url != null) {
            browserUrl = url
            browserNonce += 1
        }
        bringToFront(browserWin)
    }

    // Reload the app list on first start and every time the launcher resumes
    LaunchedEffect(refreshKey) {
        installedApps = appsManager.loadApps()
    }

    // Welcome message
    LaunchedEffect(Unit) {
        out("FLauncher Desktop OS [Version 2.1.0-cyber]", 4)
        out("Kernel Linux / Android ARM64 Desktop Shell", 4)
        out("Type 'help' for commands, or tap the dock icons below.", 3)
    }

    fun executeCommand(raw: String) {
        val cmd = raw.trim()
        if (cmd.isEmpty()) return
        out("root@flauncher # $cmd", 1)

        val parts = cmd.split(" ", limit = 2)
        val head = parts[0].lowercase()
        val arg = parts.getOrNull(1)?.trim().orEmpty()

        when (head) {
            "help" -> {
                out("Available Desktop Commands:")
                out("  terminal           bring terminal to front")
                out("  apps               open application drawer")
                out("  open <app>         launch installed application")
                out("  info <app>         open system info page for an app")
                out("  remove <app>       uninstall an app")
                out("  browser [url]      open web browser (optionally at url)")
                out("  search <query>     web search in browser")
                out("  weather [city]     open atmosphere weather")
                out("  maps / map <dest>  open cyber map")
                out("  notes / note <msg> open notes or quick save")
                out("  files              open storage file explorer")
                out("  call [number]      open the phone dialer")
                out("  notif              show recent notifications")
                out("  theme [name]       list or change desktop theme")
                out("  wallpaper          pick a system wallpaper")
                out("  home               choose default home app")
                out("  settings           desktop system settings")
                out("  battery            battery level and stats")
                out("  time               system time & date")
                out("  uptime             device uptime")
                out("  clear              clear terminal screen")
            }
            "clear" -> terminalLines.clear()
            "terminal" -> bringToFront(terminalWin)
            "apps" -> {
                bringToFront(appsWin)
                out("Opened App Drawer with ${installedApps.size} apps", 3)
            }
            "browser" -> {
                openBrowser(if (arg.isBlank()) null else normalizeUrl(arg))
                out("Opened Cyber Browser", 3)
            }
            "search" -> {
                if (arg.isBlank()) {
                    out("Usage: search <query>", 2)
                } else {
                    openBrowser("https://duckduckgo.com/?q=" + Uri.encode(arg))
                    out("Searching: '$arg'", 3)
                }
            }
            "weather" -> {
                bringToFront(weatherWin)
                out("Opened Atmosphere Weather", 3)
            }
            "maps", "map" -> {
                bringToFront(mapsWin)
                out("Opened Cyber Map", 3)
            }
            "notes" -> {
                bringToFront(notesWin)
                out("Opened Quick Notes", 3)
            }
            "note" -> {
                if (arg.isBlank()) {
                    out("Usage: note <text to save>", 2)
                } else {
                    notesManager.saveNote("Quick Note", arg)
                    bringToFront(notesWin)
                    out("Note saved successfully.", 3)
                }
            }
            "files" -> {
                bringToFront(filesWin)
                out("Opened Storage Files", 3)
            }
            "settings" -> {
                bringToFront(settingsWin)
                out("Opened Desktop Settings", 3)
            }
            "call" -> {
                val ok = startSafe(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(arg))))
                if (ok) out("Opening dialer", 3) else out("No dialer app available", 2)
            }
            "wallpaper" -> {
                val picker = Intent.createChooser(Intent(Intent.ACTION_SET_WALLPAPER), "Wallpaper")
                if (!startSafe(picker)) out("No wallpaper picker available", 2)
                else out("Pick a wallpaper, then choose the 'system wallpaper' theme in settings.", 3)
            }
            "home" -> {
                if (!startSafe(Intent(Settings.ACTION_HOME_SETTINGS))) out("Could not open home settings", 2)
            }
            "theme" -> {
                if (arg.isBlank()) {
                    out("Themes: " + WallpaperTheme.entries.joinToString(", ") { it.name.lowercase() })
                    out("Usage: theme <name>", 3)
                } else {
                    val q = arg.lowercase().replace(' ', '_')
                    val match = WallpaperTheme.entries.firstOrNull { it.name.lowercase().contains(q) }
                    if (match != null) {
                        wallpaperTheme = match
                        out("Theme set: ${match.name.lowercase()}", 3)
                    } else {
                        out("Unknown theme: $arg", 2)
                    }
                }
            }
            "notif", "notifications" -> {
                val enabled = NotificationManagerCompat.getEnabledListenerPackages(context)
                    .contains(context.packageName)
                if (!enabled) {
                    out("Notification access is off. Enable FLauncher in the list, then run 'notif' again.", 2)
                    startSafe(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                } else {
                    val items = NotificationStore.items
                    if (items.isEmpty()) {
                        out("No notifications.", 3)
                    } else {
                        items.take(8).forEach { n ->
                            out("[${n.app}] ${n.title} ${n.text}".trim())
                        }
                    }
                }
            }
            "time" -> {
                val fmt = SimpleDateFormat("EEEE, d MMMM yyyy HH:mm:ss", Locale.getDefault())
                out(fmt.format(Date()), 3)
            }
            "battery" -> {
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                val bat = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
                out("Battery Level: $bat%", 3)
            }
            "uptime" -> {
                val ms = SystemClock.uptimeMillis()
                val hrs = ms / (1000 * 60 * 60)
                val mins = (ms / (1000 * 60)) % 60
                out("System Uptime: ${hrs}h ${mins}m", 3)
            }
            "open" -> {
                val match = findApp(arg)
                if (match != null) {
                    val launched = appsManager.launchApp(match.packageName)
                    if (launched) out("Launched ${match.label}", 3)
                    else out("Failed to launch ${match.label}", 2)
                } else {
                    out("No installed app found matching '$arg'", 2)
                }
            }
            "info" -> {
                val match = findApp(arg)
                if (match != null) {
                    appsManager.openAppInfo(match.packageName)
                    out("Opened info for ${match.label}", 3)
                } else {
                    out("No installed app found matching '$arg'", 2)
                }
            }
            "remove", "uninstall" -> {
                val match = findApp(arg)
                if (match != null) {
                    appsManager.uninstallApp(match.packageName)
                    out("Uninstall prompt for ${match.label}", 3)
                } else {
                    out("No installed app found matching '$arg'", 2)
                }
            }
            else -> {
                // Try fuzzy launch app directly if typed as single word
                val match = installedApps.firstOrNull { it.label.lowercase() == head }
                    ?: installedApps.firstOrNull { it.label.lowercase().startsWith(head) }

                if (match != null) {
                    appsManager.launchApp(match.packageName)
                    out("Launched ${match.label}", 3)
                } else {
                    out("Command not found: $head (type 'help' for commands)", 2)
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        val screenW = maxWidth.value
        val screenH = maxHeight.value

        // Cyberpunk Desktop Wallpaper Background
        when (wallpaperTheme) {
            WallpaperTheme.SYSTEM_WALLPAPER -> {
                Box(modifier = Modifier.fillMaxSize().background(Color(0x33000000)))
            }
            WallpaperTheme.DARK_AURORA -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF1F1D36), Color(0xFF10121E), CyberColors.BgDark),
                                center = Offset(300f, 400f),
                                radius = 900f
                            )
                        )
                )
            }
            WallpaperTheme.DEEP_MATRIX -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF07120C), Color(0xFF091710), CyberColors.BgDark)
                            )
                        )
                )
            }
            WallpaperTheme.CHARCOAL_GLASS -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF141620), Color(0xFF0D0E15), Color(0xFF171A27))
                            )
                        )
                )
            }
            WallpaperTheme.NEON_GRID -> {
                Box(modifier = Modifier.fillMaxSize().background(CyberColors.BgDark)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val gridSpacing = 40f
                        val w = size.width
                        val h = size.height

                        // Draw subtle cyber grid
                        for (x in 0..(w / gridSpacing).toInt()) {
                            drawLine(
                                color = Color(0x0C7AA2F7),
                                start = Offset(x * gridSpacing, 0f),
                                end = Offset(x * gridSpacing, h),
                                strokeWidth = 1f
                            )
                        }
                        for (y in 0..(h / gridSpacing).toInt()) {
                            drawLine(
                                color = Color(0x0C7AA2F7),
                                start = Offset(0f, y * gridSpacing),
                                end = Offset(w, y * gridSpacing),
                                strokeWidth = 1f
                            )
                        }
                    }
                }
            }
        }

        // Layout Root: Status Bar + Desktop Workspace Area + Quick Dock + Command Bar
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Status Bar
            StatusBar(
                activeContextName = focusedWindow?.type?.name,
                modifier = Modifier.fillMaxWidth()
            )

            // Central Desktop Workspace where floating windows live
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Desktop tap unfocuses windows
                        focusedWindow = null
                    }
            ) {
                // Floating Window 1: Terminal
                WindowFrame(
                    state = terminalWin,
                    isFocused = focusedWindow == terminalWin,
                    screenWidthDp = screenW,
                    screenHeightDp = screenH,
                    onFocus = { bringToFront(terminalWin) },
                    onClose = { terminalWin.isVisible.value = false }
                ) {
                    TerminalContent(
                        lines = terminalLines,
                        onCommand = { executeCommand(it) }
                    )
                }

                // Floating Window 2: Cyber Browser
                WindowFrame(
                    state = browserWin,
                    isFocused = focusedWindow == browserWin,
                    screenWidthDp = screenW,
                    screenHeightDp = screenH,
                    onFocus = { bringToFront(browserWin) },
                    onClose = { browserWin.isVisible.value = false }
                ) {
                    BrowserContent(
                        navigateTo = browserUrl,
                        navigateNonce = browserNonce
                    )
                }

                // Floating Window 3: Atmosphere Weather
                WindowFrame(
                    state = weatherWin,
                    isFocused = focusedWindow == weatherWin,
                    screenWidthDp = screenW,
                    screenHeightDp = screenH,
                    onFocus = { bringToFront(weatherWin) },
                    onClose = { weatherWin.isVisible.value = false }
                ) {
                    WeatherContent()
                }

                // Floating Window 4: Cyber Map
                WindowFrame(
                    state = mapsWin,
                    isFocused = focusedWindow == mapsWin,
                    screenWidthDp = screenW,
                    screenHeightDp = screenH,
                    onFocus = { bringToFront(mapsWin) },
                    onClose = { mapsWin.isVisible.value = false }
                ) {
                    MapContent()
                }

                // Floating Window 5: Quick Notes
                WindowFrame(
                    state = notesWin,
                    isFocused = focusedWindow == notesWin,
                    screenWidthDp = screenW,
                    screenHeightDp = screenH,
                    onFocus = { bringToFront(notesWin) },
                    onClose = { notesWin.isVisible.value = false }
                ) {
                    NotesContent()
                }

                // Floating Window 6: Storage Files
                WindowFrame(
                    state = filesWin,
                    isFocused = focusedWindow == filesWin,
                    screenWidthDp = screenW,
                    screenHeightDp = screenH,
                    onFocus = { bringToFront(filesWin) },
                    onClose = { filesWin.isVisible.value = false }
                ) {
                    FilesContent()
                }

                // Floating Window 7: App Drawer
                WindowFrame(
                    state = appsWin,
                    isFocused = focusedWindow == appsWin,
                    screenWidthDp = screenW,
                    screenHeightDp = screenH,
                    onFocus = { bringToFront(appsWin) },
                    onClose = { appsWin.isVisible.value = false }
                ) {
                    AppsDrawerContent(
                        apps = installedApps,
                        onLaunch = { app ->
                            appsManager.launchApp(app.packageName)
                            out("Launched ${app.label}", 3)
                        }
                    )
                }

                // Floating Window 8: Desktop System Settings
                WindowFrame(
                    state = settingsWin,
                    isFocused = focusedWindow == settingsWin,
                    screenWidthDp = screenW,
                    screenHeightDp = screenH,
                    onFocus = { bringToFront(settingsWin) },
                    onClose = { settingsWin.isVisible.value = false }
                ) {
                    SystemSettingsContent(
                        currentTheme = wallpaperTheme,
                        onSelectTheme = { wallpaperTheme = it }
                    )
                }
            }

            // Bottom Quick Dock
            QuickDock(
                windows = windows,
                activeWindow = focusedWindow,
                onToggleWindow = { toggleWindow(it) },
                modifier = Modifier.fillMaxWidth()
            )

            // Bottom Command Prompt Bar
            CommandBar(
                input = commandInput,
                onInputChange = { commandInput = it },
                onSubmit = { executeCommand(it) },
                apps = installedApps,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
