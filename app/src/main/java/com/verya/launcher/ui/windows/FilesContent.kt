package com.verya.launcher.ui.windows

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.verya.launcher.model.FileItem
import com.verya.launcher.theme.CyberColors
import com.verya.launcher.theme.CyberTypography
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FilesContent() {
    val context = LocalContext.current
    val initialDir = remember {
        val ext = Environment.getExternalStorageDirectory()
        if (ext.exists() && ext.canRead()) ext else context.filesDir
    }

    var currentDir by remember { mutableStateOf(initialDir) }
    var fileList by remember { mutableStateOf(listDirectory(currentDir)) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val safLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                statusMessage = "Cannot open: ${e.message}"
            }
        }
    }

    val timeFmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberColors.BgDark)
            .padding(8.dp)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Up Directory button
            val parent = currentDir.parentFile
            val canGoUp = parent != null && parent.canRead()

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberColors.BgCard)
                    .clickable(enabled = canGoUp) {
                        if (parent != null) {
                            currentDir = parent
                            fileList = listDirectory(parent)
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Up",
                    tint = if (canGoUp) CyberColors.Blue else CyberColors.TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "..",
                    color = if (canGoUp) CyberColors.Blue else CyberColors.TextMuted,
                    fontFamily = CyberTypography.Mono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // SAF Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberColors.Highlight)
                    .border(1.dp, CyberColors.Cyan, RoundedCornerShape(4.dp))
                    .clickable { safLauncher.launch(arrayOf("*/*")) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileOpen,
                        contentDescription = "System Picker",
                        tint = CyberColors.Cyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "SAF PICKER",
                        color = CyberColors.Cyan,
                        fontFamily = CyberTypography.Mono,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Current Path
        Text(
            text = currentDir.absolutePath,
            color = CyberColors.Yellow,
            fontFamily = CyberTypography.Mono,
            fontSize = 10.sp,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberColors.BgCard, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        )

        if (statusMessage != null) {
            Text(
                text = statusMessage!!,
                color = CyberColors.Pink,
                fontFamily = CyberTypography.Mono,
                fontSize = 10.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        // Files List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(fileList) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberColors.BgCard)
                        .border(1.dp, CyberColors.GlassBorder, RoundedCornerShape(4.dp))
                        .clickable {
                            val f = File(item.path)
                            if (item.isDirectory) {
                                currentDir = f
                                fileList = listDirectory(f)
                            } else {
                                openFile(context, f) { statusMessage = it }
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                            contentDescription = null,
                            tint = if (item.isDirectory) CyberColors.Yellow else CyberColors.Cyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = item.name,
                            color = CyberColors.TextPrimary,
                            fontFamily = CyberTypography.Mono,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }

                    Text(
                        text = if (item.isDirectory) "<DIR>" else formatBytes(item.size),
                        color = CyberColors.TextMuted,
                        fontFamily = CyberTypography.Mono,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

private fun listDirectory(dir: File): List<FileItem> {
    return try {
        val files = dir.listFiles() ?: emptyArray()
        files.map {
            FileItem(
                name = it.name,
                path = it.absolutePath,
                isDirectory = it.isDirectory,
                size = if (it.isDirectory) 0 else it.length(),
                lastModified = it.lastModified()
            )
        }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    } catch (_: Exception) {
        emptyList()
    }
}

private fun openFile(context: Context, file: File, onError: (String) -> Unit) {
    try {
        val ext = file.extension.lowercase()
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"
        val uri = Uri.fromFile(file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        onError("Cannot open file: ${e.message}")
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
    return String.format(Locale.US, "%.1f %s", value, units[digitGroups])
}
