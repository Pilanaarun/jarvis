package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "USER" or "JARVIS"
    val text: String,
    val aiMode: String = "JARVIS_CORE", // JARVIS_CORE, CHATGPT_PRO, VOICE_FAST
    val timestampMs: Long = System.currentTimeMillis()
)
