package com.verya.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verya.launcher.model.AppItem
import com.verya.launcher.theme.CyberColors
import com.verya.launcher.theme.CyberTypography

@Composable
fun CommandBar(
    input: String,
    onInputChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    apps: List<AppItem>,
    modifier: Modifier = Modifier
) {
    val quickCommands = listOf("help", "apps", "browser", "weather", "maps", "notes", "files", "settings", "clear")

    val suggestions = remember(input, apps) {
        val q = input.trim().lowercase()
        if (q.isEmpty()) {
            emptyList()
        } else {
            val cmdMatches = quickCommands.filter { it.startsWith(q) }
            val appMatches = apps.filter { it.label.lowercase().contains(q) }.map { "open ${it.label}" }
            (cmdMatches + appMatches).take(6)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CyberColors.BgDark)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Suggestions row if user typing
        if (suggestions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                suggestions.forEach { sug ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberColors.BgCard)
                            .border(1.dp, CyberColors.GlassBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                onSubmit(sug)
                                onInputChange("")
                            }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "> $sug",
                            color = CyberColors.Cyan,
                            fontFamily = CyberTypography.Mono,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(CyberColors.BgCard)
                .border(1.dp, CyberColors.GlassBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "❯ ",
                color = CyberColors.TerminalPrompt,
                fontFamily = CyberTypography.Mono,
                fontSize = 13.sp
            )

            BasicTextField(
                value = input,
                onValueChange = onInputChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = CyberColors.TextPrimary,
                    fontFamily = CyberTypography.Mono,
                    fontSize = 13.sp
                ),
                cursorBrush = SolidColor(CyberColors.Cyan),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = {
                    if (input.isNotBlank()) {
                        onSubmit(input)
                        onInputChange("")
                    }
                }),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box {
                        if (input.isEmpty()) {
                            Text(
                                text = "enter command (e.g. 'weather', 'browser', 'open youtube')...",
                                color = CyberColors.TextMuted,
                                fontFamily = CyberTypography.Mono,
                                fontSize = 12.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}
