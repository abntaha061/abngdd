package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentPdfDao {
    @Query("SELECT * FROM recent_pdfs ORDER BY timestamp DESC")
    fun getAllRecentPdfs(): Flow<List<RecentPdf>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentPdf(pdf: RecentPdf)

    @Query("DELETE FROM recent_pdfs WHERE id = :id")
    suspend fun deleteRecentPdf(id: Int)

    @Query("DELETE FROM recent_pdfs")
    suspend fun clearHistory()

    @Query("SELECT * FROM recent_pdfs WHERE sourceUriOrUrl = :source LIMIT 1")
    suspend fun getPdfBySource(source: String): RecentPdf?
}
