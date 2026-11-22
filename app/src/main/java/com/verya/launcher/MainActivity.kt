package com.verya.launcher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class App(val label: String, val pkg: String)

/** kind: 0 = output, 1 = input echo, 2 = error, 3 = accent */
data class Line(val text: String, val kind: Int)

class Term(private val ctx: Context) {
    val lines = mutableStateListOf<Line>()
    var apps by mutableStateOf(emptyList<App>())
    private val prefs = ctx.getSharedPreferences("verya", Context.MODE_PRIVATE)

    init {
        out("Verya Launcher v0.1.0", 3)
        out("type 'help' for commands, or just an app name")
    }

    fun out(s: String, kind: Int = 0) {
        lines.add(Line(s, kind))
    }

    fun loadApps() {
        val pm = ctx.packageManager
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        apps = pm.queryIntentActivities(i, 0)
            .map { App(it.loadLabel(pm).toString(), it.activityInfo.packageName) }
            .filter { it.pkg != ctx.packageName }
            .distinctBy { it.pkg }
            .sortedBy { it.label.lowercase() }
    }

    fun battery(): Int {
        val bm = ctx.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    fun find(q: String): App? {
        val s = q.trim().lowercase()
        if (s.isEmpty()) return null
        return apps.firstOrNull { it.label.lowercase() == s }
            ?: apps.firstOrNull { it.label.lowercase().startsWith(s) }
            ?: apps.firstOrNull { it.label.lowercase().contains(s) }
    }

    fun launch(app: App) {
        val intent = ctx.packageManager.getLaunchIntentForPackage(app.pkg)
        if (intent == null) {
            out("cannot launch ${app.label}", 2)
            return
        }
        start(intent)
    }

    private fun start(i: Intent) {
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            ctx.startActivity(i)
        } catch (e: Exception) {
            out("failed: ${e.message}", 2)
        }
    }

    private fun notes(): List<String> =
        prefs.getString("notes", "")!!.split("\n").filter { it.isNotBlank() }

    fun run(raw: String) {
        val cmd = raw.trim()
        if (cmd.isEmpty()) return
        out("\u276F $cmd", 1)
        val parts = cmd.split(" ", limit = 2)
        val head = parts[0].lowercase()
        val arg = parts.getOrNull(1)?.trim().orEmpty()

        when (head) {
            "help" -> {
                out("apps               list installed apps")
                out("open <name>        launch an app")
                out("call [number]      open the dialer")
                out("search <text>      web search")
                out("files              open file manager")
                out("settings           system settings")
                out("info <app>         app info page")
                out("uninstall <app>    remove an app")
                out("note <text>        save a note")
                out("notes [clear]      show / clear notes")
                out("time | battery     status")
                out("clear              clear the screen")
            }
            "clear" -> lines.clear()
            "apps" -> if (apps.isEmpty()) out("no apps found", 2)
            else out(apps.joinToString("  ") { it.label })
            "open" -> {
                val a = find(arg)
                if (a != null) launch(a) else out("no app matches '$arg'", 2)
            }
            "call" -> start(
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(arg)))
            )
            "search" -> start(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/search?q=" + Uri.encode(arg))
                )
            )
            "settings" -> start(Intent(Settings.ACTION_SETTINGS))
            "files" -> {
                val a = find("files")
                if (a != null) launch(a) else out("no files app found", 2)
            }
            "info" -> {
                val a = find(arg)
                if (a != null) start(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + a.pkg)
                    )
                ) else out("no app matches '$arg'", 2)
            }
            "uninstall" -> {
                val a = find(arg)
                if (a != null) start(
                    Intent(Intent.ACTION_DELETE, Uri.parse("package:" + a.pkg))
                ) else out("no app matches '$arg'", 2)
            }
            "note" -> {
                if (arg.isEmpty()) {
                    out("usage: note <text>", 2)
                } else {
                    prefs.edit().putString("notes", (notes() + arg).joinToString("\n")).apply()
                    out("saved", 3)
                }
            }
            "notes" -> {
                if (arg == "clear") {
                    prefs.edit().remove("notes").apply()
                    out("notes cleared", 3)
                } else {
                    val n = notes()
                    if (n.isEmpty()) out("no notes yet") else n.forEachIndexed { i, s -> out("${i + 1}. $s") }
                }
            }
            "time" -> out(SimpleDateFormat("EEE d MMM yyyy  HH:mm", Locale.getDefault()).format(Date()))
            "battery" -> out("battery: ${battery()}%")
            else -> {
                val a = find(cmd)
                if (a != null) launch(a) else out("command not found: $head", 2)
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var term: Term

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        term = Term(applicationContext)
        term.loadApps()
        setContent { Screen(term) }
    }

    override fun onResume() {
        super.onResume()
        term.loadApps()
    }
}

private val Fg = Color(0xFFC0CAF5)
private val Dim = Color(0xFF7A88B8)
private val Prompt = Color(0xFFBB9AF7)
private val Accent = Color(0xFF9ECE6A)
private val Err = Color(0xFFF7768E)
private val Scrim = Color(0x88000000)
private val Mono = FontFamily.Monospace

@Composable
fun Screen(t: Term) {
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val status by produceState("") {
        val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            value = "${t.battery()}%  ${fmt.format(Date())}"
            delay(1000)
        }
    }
    LaunchedEffect(t.lines.size) {
        if (t.lines.isNotEmpty()) listState.animateScrollToItem(t.lines.lastIndex)
    }
    val suggestions = remember(input, t.apps) {
        val q = input.trim().lowercase().removePrefix("open ").trim()
        if (q.isEmpty()) emptyList() else t.apps.filter { it.label.lowercase().contains(q) }.take(8)
    }

    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 10.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Scrim)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("root@verya:~", color = Fg, fontFamily = Mono, fontSize = 12.sp)
            Text(status, color = Dim, fontFamily = Mono, fontSize = 12.sp)
        }

        Row(Modifier.weight(1f).padding(top = 6.dp)) {
            LazyColumn(
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(Scrim)
                    .padding(8.dp),
                state = listState
            ) {
                items(t.lines) { l ->
                    Text(
                        l.text,
                        color = when (l.kind) {
                            1 -> Prompt
                            2 -> Err
                            3 -> Accent
                            else -> Fg
                        },
                        fontFamily = Mono,
                        fontSize = 13.sp
                    )
                }
            }
            Column(
                Modifier.padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Bottom),
                horizontalAlignment = Alignment.End
            ) {
                listOf("apps", "notes", "call", "files", "settings", "clear", "help").forEach { c ->
                    Text(
                        c,
                        color = Fg,
                        fontFamily = Mono,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .background(Scrim, RoundedCornerShape(4.dp))
                            .border(1.dp, Dim, RoundedCornerShape(4.dp))
                            .clickable { t.run(c) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (suggestions.isNotEmpty()) {
            LazyRow(
                Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(suggestions) { a ->
                    Text(
                        a.label,
                        color = Accent,
                        fontFamily = Mono,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .background(Scrim, RoundedCornerShape(12.dp))
                            .border(1.dp, Accent, RoundedCornerShape(12.dp))
                            .clickable {
                                t.launch(a)
                                input = ""
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(Scrim, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("\u276F ", color = Prompt, fontFamily = Mono, fontSize = 14.sp)
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                singleLine = true,
                textStyle = TextStyle(color = Fg, fontFamily = Mono, fontSize = 14.sp),
                cursorBrush = SolidColor(Accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = {
                    t.run(input)
                    input = ""
                }),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box {
                        if (input.isEmpty()) {
                            Text(
                                "type a command or app name...",
                                color = Dim,
                                fontFamily = Mono,
                                fontSize = 14.sp
                            )
                        }
                        inner()
                    }
                }
            )
        }
    }
}
