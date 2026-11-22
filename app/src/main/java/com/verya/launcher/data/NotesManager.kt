package com.verya.launcher.data

import android.content.Context
import com.verya.launcher.model.NoteItem
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class NotesManager(context: Context) {
    private val prefs = context.getSharedPreferences("flauncher_notes_db", Context.MODE_PRIVATE)

    fun getNotes(): List<NoteItem> {
        val raw = prefs.getString("notes_json", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<NoteItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    NoteItem(
                        id = obj.getString("id"),
                        title = obj.optString("title", "Untitled Note"),
                        content = obj.optString("content", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveNote(title: String, content: String, existingId: String? = null): NoteItem {
        val currentNotes = getNotes().toMutableList()
        val id = existingId ?: UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val item = NoteItem(id, title.ifBlank { "Untitled Note" }, content, timestamp)

        val idx = currentNotes.indexOfFirst { it.id == id }
        if (idx >= 0) {
            currentNotes[idx] = item
        } else {
            currentNotes.add(0, item)
        }

        persist(currentNotes)
        return item
    }

    fun deleteNote(id: String) {
        val currentNotes = getNotes().filter { it.id != id }
        persist(currentNotes)
    }

    fun clearAll() {
        prefs.edit().remove("notes_json").apply()
    }

    private fun persist(notes: List<NoteItem>) {
        val arr = JSONArray()
        for (n in notes) {
            val obj = JSONObject()
            obj.put("id", n.id)
            obj.put("title", n.title)
            obj.put("content", n.content)
            obj.put("timestamp", n.timestamp)
            arr.put(obj)
        }
        prefs.edit().putString("notes_json", arr.toString()).apply()
    }
}
