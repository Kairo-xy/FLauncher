package com.verya.launcher.ui.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verya.launcher.data.NotesManager
import com.verya.launcher.model.NoteItem
import com.verya.launcher.theme.CyberColors
import com.verya.launcher.theme.CyberTypography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesContent() {
    val context = LocalContext.current
    val notesManager = remember { NotesManager(context) }
    var notesList by remember { mutableStateOf(notesManager.getNotes()) }
    var searchQuery by remember { mutableStateOf("") }

    // Editor state
    var editingNote by remember { mutableStateOf<NoteItem?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var editTitle by remember { mutableStateOf("") }
    var editContent by remember { mutableStateOf("") }

    val filteredNotes = remember(notesList, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) notesList else {
            notesList.filter {
                it.title.lowercase().contains(q) || it.content.lowercase().contains(q)
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
        if (isCreatingNew || editingNote != null) {
            // Note Editor View
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                Row(
                    modifier = Modifier.clickable {
                        isCreatingNew = false
                        editingNote = null
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = CyberColors.Blue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "BACK",
                        color = CyberColors.Blue,
                        fontFamily = CyberTypography.Mono,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Save button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberColors.Highlight)
                        .border(1.dp, CyberColors.Cyan, RoundedCornerShape(4.dp))
                        .clickable {
                            val title = editTitle.ifBlank { "Untitled Note" }
                            notesManager.saveNote(title, editContent, editingNote?.id)
                            notesList = notesManager.getNotes()
                            isCreatingNew = false
                            editingNote = null
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = CyberColors.Cyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "SAVE",
                            color = CyberColors.Cyan,
                            fontFamily = CyberTypography.Mono,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Title input
            BasicTextField(
                value = editTitle,
                onValueChange = { editTitle = it },
                singleLine = true,
                textStyle = TextStyle(
                    color = CyberColors.Cyan,
                    fontFamily = CyberTypography.Mono,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                cursorBrush = SolidColor(CyberColors.Cyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberColors.BgCard, RoundedCornerShape(4.dp))
                    .border(1.dp, CyberColors.GlassBorder, RoundedCornerShape(4.dp))
                    .padding(8.dp),
                decorationBox = { inner ->
                    if (editTitle.isEmpty()) {
                        Text(
                            text = "Note title...",
                            color = CyberColors.TextMuted,
                            fontFamily = CyberTypography.Mono,
                            fontSize = 14.sp
                        )
                    }
                    inner()
                }
            )

            // Content input
            BasicTextField(
                value = editContent,
                onValueChange = { editContent = it },
                textStyle = TextStyle(
                    color = CyberColors.TextPrimary,
                    fontFamily = CyberTypography.Mono,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                cursorBrush = SolidColor(CyberColors.Cyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 8.dp)
                    .background(CyberColors.BgCard, RoundedCornerShape(4.dp))
                    .border(1.dp, CyberColors.GlassBorder, RoundedCornerShape(4.dp))
                    .padding(8.dp),
                decorationBox = { inner ->
                    if (editContent.isEmpty()) {
                        Text(
                            text = "Write note content here...",
                            color = CyberColors.TextMuted,
                            fontFamily = CyberTypography.Mono,
                            fontSize = 12.sp
                        )
                    }
                    inner()
                }
            )

        } else {
            // Notes List View
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search bar
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberColors.BgCard)
                        .border(1.dp, CyberColors.GlassBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = CyberColors.Cyan,
                        modifier = Modifier.size(14.dp)
                    )
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = CyberColors.TextPrimary,
                            fontFamily = CyberTypography.Mono,
                            fontSize = 11.sp
                        ),
                        cursorBrush = SolidColor(CyberColors.Cyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "search notes...",
                                    color = CyberColors.TextMuted,
                                    fontFamily = CyberTypography.Mono,
                                    fontSize = 11.sp
                                )
                            }
                            inner()
                        }
                    )
                }

                // Add Note Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberColors.Highlight)
                        .border(1.dp, CyberColors.Cyan, RoundedCornerShape(4.dp))
                        .clickable {
                            editTitle = ""
                            editContent = ""
                            editingNote = null
                            isCreatingNew = true
                        }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New",
                            tint = CyberColors.Cyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "NEW",
                            color = CyberColors.Cyan,
                            fontFamily = CyberTypography.Mono,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "No notes yet. Tap '+ NEW' to create one." else "No notes match '$searchQuery'",
                        color = CyberColors.TextMuted,
                        fontFamily = CyberTypography.Mono,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberColors.BgCard)
                                .border(1.dp, CyberColors.GlassBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    editingNote = note
                                    editTitle = note.title
                                    editContent = note.content
                                }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = note.title,
                                    color = CyberColors.Cyan,
                                    fontFamily = CyberTypography.Mono,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                if (note.content.isNotBlank()) {
                                    Text(
                                        text = note.content,
                                        color = CyberColors.TextSecondary,
                                        fontFamily = CyberTypography.Mono,
                                        fontSize = 11.sp,
                                        maxLines = 2,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = timeFmt.format(Date(note.timestamp)),
                                    color = CyberColors.TextMuted,
                                    fontFamily = CyberTypography.Mono,
                                    fontSize = 9.sp
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = CyberColors.Pink,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable {
                                        notesManager.deleteNote(note.id)
                                        notesList = notesManager.getNotes()
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}
