package com.verya.launcher.ui.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verya.launcher.model.TermLine
import com.verya.launcher.theme.CyberColors
import com.verya.launcher.theme.CyberTypography

@Composable
fun TerminalContent(
    lines: List<TermLine>,
    onCommand: (String) -> Unit
) {
    var cmdInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.lastIndex)
        }
    }

    val quickChips = listOf("help", "apps", "browser", "weather", "maps", "notes", "files", "battery", "clear")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberColors.BgDark)
            .padding(6.dp)
    ) {
        // Output log
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(4.dp),
            state = listState
        ) {
            items(lines) { line ->
                Text(
                    text = line.text,
                    color = when (line.kind) {
                        1 -> CyberColors.TerminalPrompt
                        2 -> CyberColors.Pink
                        3 -> CyberColors.Green
                        4 -> CyberColors.Cyan
                        else -> CyberColors.TextPrimary
                    },
                    fontFamily = CyberTypography.Mono,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Quick chip shortcuts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            quickChips.forEach { chip ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberColors.BgCard)
                        .border(1.dp, CyberColors.GlassBorder, RoundedCornerShape(4.dp))
                        .clickable { onCommand(chip) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = chip,
                        color = CyberColors.Blue,
                        fontFamily = CyberTypography.Mono,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Terminal input line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberColors.BgCard, RoundedCornerShape(4.dp))
                .border(1.dp, CyberColors.GlassBorder, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "root@flauncher # ",
                color = CyberColors.TerminalPrompt,
                fontFamily = CyberTypography.Mono,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            BasicTextField(
                value = cmdInput,
                onValueChange = { cmdInput = it },
                singleLine = true,
                textStyle = TextStyle(
                    color = CyberColors.TextPrimary,
                    fontFamily = CyberTypography.Mono,
                    fontSize = 12.sp
                ),
                cursorBrush = SolidColor(CyberColors.Cyan),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = {
                    if (cmdInput.isNotBlank()) {
                        onCommand(cmdInput)
                        cmdInput = ""
                    }
                }),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
