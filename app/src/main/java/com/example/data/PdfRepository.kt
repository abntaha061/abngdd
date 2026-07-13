package com.example.data

import kotlinx.coroutines.flow.Flow

class PdfRepository(private val recentPdfDao: RecentPdfDao) {
    val allRecentPdfs: Flow<List<RecentPdf>> = recentPdfDao.getAllRecentPdfs()

    suspend fun insert(pdf: RecentPdf) {
        // If there's already an item with the same source, delete it first to push this new entry to the top of the history
        val existing = recentPdfDao.getPdfBySource(pdf.sourceUriOrUrl)
        if (existing != null) {
            recentPdfDao.deleteRecentPdf(existing.id)
        }
        recentPdfDao.insertRecentPdf(pdf)
    }

    suspend fun deleteById(id: Int) {
        recentPdfDao.deleteRecentPdf(id)
    }

    suspend fun clearAll() {
        recentPdfDao.clearHistory()
    }
}
