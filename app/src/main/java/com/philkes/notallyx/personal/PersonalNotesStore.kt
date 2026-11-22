package com.philkes.notallyx.personal

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class PersonalMemo(val id: Long, val title: String, val body: String)

data class PersonalTask(val id: Long, val body: String, val completed: Boolean)

/**
 * Private on-device SQLite storage for the simplified two-tab experience.
 *
 * This is deliberately separate from NotallyX's existing database: no existing
 * notes are migrated, changed or deleted by this feature.
 */
class PersonalNotesStore(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "personal_notes.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE personal_memos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                body TEXT NOT NULL
            )"""
        )
        db.execSQL(
            """CREATE TABLE personal_tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                body TEXT NOT NULL,
                completed INTEGER NOT NULL DEFAULT 0
            )"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Add explicit migrations here when the personal schema changes.
        // Never drop these tables: they contain the user's notes and tasks.
        if (oldVersion != newVersion) {
            throw IllegalStateException("Missing personal notes migration: $oldVersion to $newVersion")
        }
    }

    fun getMemos(): List<PersonalMemo> {
        val result = mutableListOf<PersonalMemo>()
        readableDatabase.rawQuery(
            "SELECT id, title, body FROM personal_memos ORDER BY id DESC", null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += PersonalMemo(
                    cursor.getLong(0), cursor.getString(1), cursor.getString(2)
                )
            }
        }
        return result
    }

    fun getMemo(id: Long): PersonalMemo? {
        readableDatabase.rawQuery(
            "SELECT id, title, body FROM personal_memos WHERE id = ?",
            arrayOf(id.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                PersonalMemo(cursor.getLong(0), cursor.getString(1), cursor.getString(2))
            } else null
        }
    }

    fun saveMemo(id: Long?, enteredTitle: String, enteredBody: String): Long {
        val body = enteredBody.trimEnd()
        val title = enteredTitle.trim() // A blank title is displayed from the first sentence.
        val values = ContentValues().apply {
            put("title", title)
            put("body", body)
        }
        if (id != null && id > 0) {
            writableDatabase.update("personal_memos", values, "id = ?", arrayOf(id.toString()))
            return id
        }
        return writableDatabase.insertOrThrow("personal_memos", null, values)
    }

    fun deleteMemo(id: Long) {
        writableDatabase.delete("personal_memos", "id = ?", arrayOf(id.toString()))
    }

    fun getTasks(): List<PersonalTask> {
        val result = mutableListOf<PersonalTask>()
        readableDatabase.rawQuery(
            "SELECT id, body, completed FROM personal_tasks ORDER BY completed ASC, id DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += PersonalTask(
                    cursor.getLong(0), cursor.getString(1), cursor.getInt(2) != 0
                )
            }
        }
        return result
    }

    fun addTask(body: String) {
        val text = body.trim()
        if (text.isEmpty()) return
        val values = ContentValues().apply {
            put("body", text)
            put("completed", 0)
        }
        writableDatabase.insertOrThrow("personal_tasks", null, values)
    }

    fun updateTask(id: Long, body: String) {
        val text = body.trim()
        if (text.isEmpty()) return
        val values = ContentValues().apply { put("body", text) }
        writableDatabase.update("personal_tasks", values, "id = ?", arrayOf(id.toString()))
    }

    fun setTaskCompleted(id: Long, completed: Boolean) {
        val values = ContentValues().apply { put("completed", if (completed) 1 else 0) }
        writableDatabase.update("personal_tasks", values, "id = ?", arrayOf(id.toString()))
    }

    fun deleteTask(id: Long) {
        writableDatabase.delete("personal_tasks", "id = ?", arrayOf(id.toString()))
    }

    companion object {
        /** Blank title means the first sentence of the memo becomes its title. */
        fun titleFromFirstSentence(body: String): String {
            val firstLine = body.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
            if (firstLine.isBlank()) return "无标题"
            val terminator = firstLine.indexOfAny(charArrayOf('。', '！', '？', '.', '!', '?'))
            val sentence = if (terminator < 0) firstLine else firstLine.substring(0, terminator + 1)
            return sentence.trim().take(80).ifBlank { "无标题" }
        }
    }
}
