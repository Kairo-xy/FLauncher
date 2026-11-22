package com.verya.launcher.ui.windows

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.verya.launcher.data.AppsManager
import com.verya.launcher.model.AppItem
import com.verya.launcher.theme.CyberColors
import com.verya.launcher.theme.CyberTypography

@Composable
fun AppsDrawerContent(
    apps: List<AppItem>,
    onLaunch: (AppItem) -> Unit
) {
    val context = LocalContext.current
    val appsManager = remember { AppsManager(context) }
    var search by remember { mutableStateOf("") }

    val filtered = remember(apps, search) {
        val q = search.trim().lowercase()
        if (q.isEmpty()) apps else apps.filter {
            it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberColors.BgDark)
            .padding(8.dp)
    ) {
        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(CyberColors.BgCard)
                .border(1.dp, CyberColors.GlassBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = CyberColors.Cyan,
                modifier = Modifier.size(16.dp)
            )
            BasicTextField(
                value = search,
                onValueChange = { search = it },
                singleLine = true,
                textStyle = TextStyle(
                    color = CyberColors.TextPrimary,
                    fontFamily = CyberTypography.Mono,
                    fontSize = 12.sp
                ),
                cursorBrush = SolidColor(CyberColors.Cyan),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp),
                decorationBox = { inner ->
                    if (search.isEmpty()) {
                        Text(
                            text = "search ${apps.size} installed apps...",
                            color = CyberColors.TextMuted,
                            fontFamily = CyberTypography.Mono,
                            fontSize = 12.sp
                        )
                    }
                    inner()
                }
            )
        }

        // App Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 72.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.packageName }) { app ->
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberColors.BgCard)
                        .border(1.dp, CyberColors.GlassBorder, RoundedCornerShape(8.dp))
                        .clickable { onLaunch(app) }
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val bitmap = remember(app.packageName) {
                        try {
                            app.icon?.toBitmap(96, 96, Bitmap.Config.ARGB_8888)?.asImageBitmap()
                        } catch (_: Exception) {
                            null
                        }
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = app.label,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = app.label,
                            tint = CyberColors.Cyan,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = app.label,
                        color = CyberColors.TextPrimary,
                        fontFamily = CyberTypography.Mono,
                        fontSize = 10.sp,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
