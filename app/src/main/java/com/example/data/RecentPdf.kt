package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_pdfs")
data class RecentPdf(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val sourceUriOrUrl: String,
    val cachedFilePath: String,
    val fileSize: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val lastReadPage: Int = 1
)
