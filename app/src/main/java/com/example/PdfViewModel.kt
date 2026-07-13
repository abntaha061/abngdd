package com.example

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.PdfRepository
import com.example.data.RecentPdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class PdfViewModel(private val repository: PdfRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentPdfToView = MutableStateFlow<RecentPdf?>(null)
    val currentPdfToView: StateFlow<RecentPdf?> = _currentPdfToView.asStateFlow()

    val recentPdfs: StateFlow<List<RecentPdf>> = repository.allRecentPdfs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun resetCurrentPdf() {
        _currentPdfToView.value = null
    }

    fun clearError() {
        _error.value = null
    }

    fun viewRecentPdf(context: Context, pdf: RecentPdf) {
        val file = File(pdf.cachedFilePath)
        if (file.exists()) {
            _currentPdfToView.value = pdf
            viewModelScope.launch {
                repository.insert(pdf.copy(timestamp = System.currentTimeMillis()))
            }
        } else {
            if (pdf.sourceUriOrUrl.startsWith("http://") || pdf.sourceUriOrUrl.startsWith("https://")) {
                _error.value = "الملف المخزن مؤقتاً غير موجود. جارٍ إعادة تحميله..."
                downloadRemotePdf(context, pdf.sourceUriOrUrl, pdf.title)
            } else {
                _error.value = "عذراً، الملف غير موجود في الذاكرة المؤقتة، يرجى إعادة تحديده."
                viewModelScope.launch {
                    repository.deleteById(pdf.id)
                }
            }
        }
    }

    fun importLocalPdf(context: Context, uri: Uri) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val resolvedNameAndSize = withContext(Dispatchers.IO) {
                    var name = "local_file.pdf"
                    var size = 0L
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (cursor.moveToFirst()) {
                            if (nameIndex != -1) {
                                name = cursor.getString(nameIndex)
                            }
                            if (sizeIndex != -1) {
                                size = cursor.getWrappedCursor()?.getLong(sizeIndex) ?: cursor.getLong(sizeIndex)
                            }
                        }
                    }
                    Pair(name, size)
                }

                val originalName = resolvedNameAndSize.first
                val size = resolvedNameAndSize.second

                val timestamp = System.currentTimeMillis()
                val cacheFileName = "pdf_${timestamp}.pdf"

                val cachedFile = withContext(Dispatchers.IO) {
                    val file = File(getPdfCacheDir(context), cacheFileName)
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        file.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    } ?: throw IOException("تعذر فتح ملف الإدخال")
                    file
                }

                val finalSize = if (size == 0L) cachedFile.length() else size

                val recentPdf = RecentPdf(
                    title = originalName,
                    sourceUriOrUrl = uri.toString(),
                    cachedFilePath = cachedFile.absolutePath,
                    fileSize = finalSize,
                    timestamp = timestamp
                )

                repository.insert(recentPdf)
                _currentPdfToView.value = recentPdf

            } catch (e: Exception) {
                _error.value = "فشل استيراد الملف: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun android.database.Cursor.getWrappedCursor(): android.database.Cursor? {
        return null
    }

    fun downloadRemotePdf(context: Context, urlString: String, customTitle: String? = null) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val (cachedFile, finalTitle) = withContext(Dispatchers.IO) {
                    val url = URL(urlString)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 10000
                    connection.readTimeout = 15000
                    connection.connect()

                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        throw IOException("استجابة الخادم: ${connection.responseCode}")
                    }

                    var fileName = customTitle ?: ""
                    if (fileName.isEmpty()) {
                        val disposition = connection.getHeaderField("Content-Disposition")
                        if (disposition != null && disposition.contains("filename=")) {
                            val index = disposition.indexOf("filename=")
                            fileName = disposition.substring(index + 9).replace("\"", "").trim()
                        }
                        if (fileName.isEmpty()) {
                            fileName = urlString.substringAfterLast("/").substringBefore("?")
                        }
                        if (fileName.isEmpty() || !fileName.endsWith(".pdf", ignoreCase = true)) {
                            fileName = "web_document.pdf"
                        }
                    }
                    if (!fileName.endsWith(".pdf", ignoreCase = true)) {
                        fileName += ".pdf"
                    }

                    val timestamp = System.currentTimeMillis()
                    val cacheFileName = "pdf_${timestamp}.pdf"
                    val file = File(getPdfCacheDir(context), cacheFileName)

                    connection.inputStream.use { inputStream ->
                        file.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    Pair(file, fileName)
                }

                val recentPdf = RecentPdf(
                    title = finalTitle,
                    sourceUriOrUrl = urlString,
                    cachedFilePath = cachedFile.absolutePath,
                    fileSize = cachedFile.length(),
                    timestamp = System.currentTimeMillis()
                )

                repository.insert(recentPdf)
                _currentPdfToView.value = recentPdf

            } catch (e: Exception) {
                _error.value = "فشل تحميل الملف: ${e.localizedMessage ?: "تحقق من اتصالك بالإنترنت"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePdfHistory(id: Int, cachedFilePath: String) {
        viewModelScope.launch {
            repository.deleteById(id)
            withContext(Dispatchers.IO) {
                val file = File(cachedFilePath)
                if (file.exists()) {
                    file.delete()
                }
            }
        }
    }

    private fun getPdfCacheDir(context: Context): File {
        return File(context.filesDir, "pdf_cache").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    fun clearAllHistory(context: Context) {
        viewModelScope.launch {
            repository.clearAll()
            withContext(Dispatchers.IO) {
                val files = getPdfCacheDir(context).listFiles { _, name -> name.startsWith("pdf_") && name.endsWith(".pdf") }
                files?.forEach { it.delete() }
            }
        }
    }
}

class PdfViewModelFactory(private val repository: PdfRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PdfViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PdfViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
