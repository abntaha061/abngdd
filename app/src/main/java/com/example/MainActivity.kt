package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.JavascriptInterface
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.ViewSidebar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.webkit.WebViewAssetLoader
import com.example.data.AppDatabase
import com.example.data.PdfRepository
import com.example.data.RecentPdf
import com.example.ui.theme.MyApplicationTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: PdfViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Pre-create WebView Code Cache directories to prevent Chromium error logs
        try {
            val codeCacheJsDir = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/js")
            val codeCacheWasmDir = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm")
            if (!codeCacheJsDir.exists()) {
                codeCacheJsDir.mkdirs()
            }
            if (!codeCacheWasmDir.exists()) {
                codeCacheWasmDir.mkdirs()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Copy sample.pdf from assets to cache directory on start
        val pdfCacheDir = File(filesDir, "pdf_cache").apply {
            if (!exists()) {
                mkdirs()
            }
        }
        val destFile = File(pdfCacheDir, "sample.pdf")
        if (!destFile.exists()) {
            try {
                assets.open("sample/sample.pdf").use { inputStream ->
                    destFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Room and Repo initialisation
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = PdfRepository(database.recentPdfDao())
        val factory = PdfViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[PdfViewModel::class.java]

        handleIntent(intent)

        setContent {
            MyApplicationTheme {
                val currentPdf by viewModel.currentPdfToView.collectAsStateWithLifecycle()
                val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
                val errorMessage by viewModel.error.collectAsStateWithLifecycle()
                val context = LocalContext.current

                // Handle error messages with native Toast feedback
                LaunchedEffect(errorMessage) {
                    errorMessage?.let {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                        viewModel.clearError()
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (currentPdf == null) {
                        HomeDashboard(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        PdfReaderScreen(
                            pdf = currentPdf!!,
                            viewModel = viewModel,
                            onBack = { viewModel.resetCurrentPdf() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Global loading spinner overlay
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f))
                                .clickable(enabled = false) {},
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "جاري تحضير ملف PDF...",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null && intent.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            if (uri != null) {
                viewModel.importLocalPdf(this, uri)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboard(
    viewModel: PdfViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recentPdfs by viewModel.recentPdfs.collectAsStateWithLifecycle()
    var urlInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Set up file picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importLocalPdf(context, it)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "قارئ ومستعرض PDF",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = WindowInsets.statusBars.asPaddingValues()
        ) {
            // Elegant Header/Onboarding Banner Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .drawBehind {
                            val colors = listOf(Color(0xFF0F4C5C), Color(0xFF118AB2))
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = colors,
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, size.height)
                                )
                            )
                        }
                        .padding(24.dp)
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF Icon Logo",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "مرحباً بك في قارئ ومستعرض PDF الذكي",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "افتح ملفات PDF من جهازك أو من أي رابط إنترنت بسرعة وأمان باستخدام pdf.js المتطور.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Quick Import Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("import_card")
                        .clickable { filePickerLauncher.launch(arrayOf("application/pdf")) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileOpen,
                                contentDescription = "استيراد ملف",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "اختر ملف PDF من الجهاز",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "تصفح وافتح ملفاتك المحلية مباشرة",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Open from URL Input Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "رمز الرابط",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "افتح ملف PDF من رابط مباشر",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            placeholder = { Text("أدخل رابط PDF المباشر هنا...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("url_input"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Go
                            ),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    if (urlInput.isNotBlank()) {
                                        viewModel.downloadRemotePdf(context, urlInput.trim())
                                        keyboardController?.hide()
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            trailingIcon = {
                                if (urlInput.isNotBlank()) {
                                    IconButton(onClick = { urlInput = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "مسح النص"
                                        )
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (urlInput.isNotBlank()) {
                                    viewModel.downloadRemotePdf(context, urlInput.trim())
                                    keyboardController?.hide()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("open_url_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(text = "تحميل وفتح الملف", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        // Presets/Samples for testing
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "روابط تجريبية سريعة للاختبار:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        SamplePdfRow(
                            title = "الملف التجريبي المضمن (sample.pdf)",
                            onClick = {
                                val pdfCacheDir = File(context.filesDir, "pdf_cache")
                                val sampleFile = File(pdfCacheDir, "sample.pdf")
                                if (sampleFile.exists()) {
                                    viewModel.viewRecentPdf(context, RecentPdf(
                                        title = "الملف التجريبي المضمن (sample.pdf)",
                                        sourceUriOrUrl = "sample.pdf",
                                        cachedFilePath = sampleFile.absolutePath,
                                        fileSize = sampleFile.length(),
                                        timestamp = System.currentTimeMillis()
                                    ))
                                } else {
                                    Toast.makeText(context, "لم يتم العثور على الملف التجريبي", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SamplePdfRow(
                            title = "دليل البدء السريع (DUMMY PDF)",
                            onClick = {
                                urlInput = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
                                viewModel.downloadRemotePdf(context, urlInput, "دليل البدء السريع")
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SamplePdfRow(
                            title = "وثيقة نموذجية ممتدة (Sample PDF)",
                            onClick = {
                                urlInput = "https://unec.edu.az/application/uploads/2014/12/pdf-sample.pdf"
                                viewModel.downloadRemotePdf(context, urlInput, "وثيقة نموذجية ممتدة")
                            }
                        )
                    }
                }
            }

            // Recent Documents Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "السجل",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "المستندات الأخيرة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    if (recentPdfs.isNotEmpty()) {
                        Text(
                            text = "مسح الكل",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .testTag("clear_history_button")
                                .clickable { viewModel.clearAllHistory(context) }
                        )
                    }
                }
            }

            // History Empty State or items
            if (recentPdfs.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = "لا يوجد ملفات",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "السجل فارغ حالياً",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ابدأ بفتح ملف PDF من جهازك أو من رابط لعرضه والاحتفاظ به هنا للوصول السريع.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                items(recentPdfs) { pdf ->
                    RecentPdfItem(
                        pdf = pdf,
                        onOpen = { viewModel.viewRecentPdf(context, pdf) },
                        onDelete = { viewModel.deletePdfHistory(pdf.id, pdf.cachedFilePath) }
                    )
                }
            }
        }
    }
}

@Composable
fun SamplePdfRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.PictureAsPdf,
            contentDescription = "ملف عينة",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RecentPdfItem(
    pdf: RecentPdf,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
    val dateStr = remember(pdf.timestamp) { formatter.format(Date(pdf.timestamp)) }
    val sizeStr = remember(pdf.fileSize) {
        if (pdf.fileSize > 1024 * 1024) {
            String.format(Locale.getDefault(), "%.2f MB", pdf.fileSize / (1024f * 1024f))
        } else {
            String.format(Locale.getDefault(), "%.0f KB", pdf.fileSize / 1024f)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("pdf_item_card")
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = "مستند",
                tint = Color(0xFFE53935), // Red typical PDF color
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pdf.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sizeStr,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "حذف من السجل",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun PdfBottomBar(
    currentPage: Int,
    totalPages: Int,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onRotate: () -> Unit,
    onToggleSidebar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Toggle Sidebar Button
            IconButton(
                onClick = onToggleSidebar,
                modifier = Modifier.testTag("bottom_toggle_sidebar")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ViewSidebar,
                    contentDescription = "القائمة الجانبية",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Zoom Out Button
            IconButton(
                onClick = onZoomOut,
                modifier = Modifier.testTag("bottom_zoom_out")
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOut,
                    contentDescription = "تصغير",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Zoom In Button
            IconButton(
                onClick = onZoomIn,
                modifier = Modifier.testTag("bottom_zoom_in")
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "تكبير",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Rotate Page Button
            IconButton(
                onClick = onRotate,
                modifier = Modifier.testTag("bottom_rotate")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.RotateRight,
                    contentDescription = "تدوير الصفحة",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Page Navigation (Prev - Number - Next)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = onPrevPage,
                    enabled = currentPage > 1,
                    modifier = Modifier.testTag("bottom_prev_page")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "الصفحة السابقة",
                        tint = if (currentPage > 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }

                Text(
                    text = "$currentPage / $totalPages",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 4.dp).testTag("bottom_page_number")
                )

                IconButton(
                    onClick = onNextPage,
                    enabled = currentPage < totalPages,
                    modifier = Modifier.testTag("bottom_next_page")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "الصفحة التالية",
                        tint = if (currentPage < totalPages) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}

class PdfWebViewState {
    var webView: WebView? = null

    fun nextPage() {
        webView?.post {
            webView?.evaluateJavascript(
                """
                (function() {
                    if (window.PDFViewerApplication && window.PDFViewerApplication.pdfViewer) {
                        window.PDFViewerApplication.pdfViewer.nextPage();
                    }
                })();
                """.trimIndent(),
                null
            )
        }
    }

    fun prevPage() {
        webView?.post {
            webView?.evaluateJavascript(
                """
                (function() {
                    if (window.PDFViewerApplication && window.PDFViewerApplication.pdfViewer) {
                        window.PDFViewerApplication.pdfViewer.previousPage();
                    }
                })();
                """.trimIndent(),
                null
            )
        }
    }

    fun zoomIn() {
        webView?.post {
            webView?.evaluateJavascript(
                """
                (function() {
                    if (window.PDFViewerApplication && window.PDFViewerApplication.pdfViewer) {
                        window.PDFViewerApplication.pdfViewer.currentScale += 0.25;
                    }
                })();
                """.trimIndent(),
                null
            )
        }
    }

    fun zoomOut() {
        webView?.post {
            webView?.evaluateJavascript(
                """
                (function() {
                    if (window.PDFViewerApplication && window.PDFViewerApplication.pdfViewer) {
                        window.PDFViewerApplication.pdfViewer.currentScale = Math.max(0.25, window.PDFViewerApplication.pdfViewer.currentScale - 0.25);
                    }
                })();
                """.trimIndent(),
                null
            )
        }
    }

    fun openFindBar() {
        webView?.post {
            webView?.evaluateJavascript(
                """
                (function() {
                    if (window.PDFViewerApplication && window.PDFViewerApplication.findBar) {
                        window.PDFViewerApplication.findBar.toggle();
                    }
                })();
                """.trimIndent(),
                null
            )
        }
    }

    fun performSearch(query: String, caseSensitive: Boolean) {
        webView?.post {
            val escapedQuery = query.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")
            webView?.evaluateJavascript(
                """
                (function() {
                    if (window.PDFViewerApplication && window.PDFViewerApplication.eventBus) {
                        window.PDFViewerApplication.eventBus.dispatch('find', {
                            type: '',
                            query: "$escapedQuery",
                            caseSensitive: $caseSensitive,
                            entireWord: false,
                            highlightAll: true,
                            findPrevious: false,
                            matchDiacritics: true
                        });
                    }
                })();
                """.trimIndent(),
                null
            )
        }
    }

    fun searchNext(query: String, caseSensitive: Boolean) {
        webView?.post {
            val escapedQuery = query.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")
            webView?.evaluateJavascript(
                """
                (function() {
                    if (window.PDFViewerApplication && window.PDFViewerApplication.eventBus) {
                        window.PDFViewerApplication.eventBus.dispatch('find', {
                            type: 'again',
                            query: "$escapedQuery",
                            caseSensitive: $caseSensitive,
                            entireWord: false,
                            highlightAll: true,
                            findPrevious: false,
                            matchDiacritics: true
                        });
                    }
                })();
                """.trimIndent(),
                null
            )
        }
    }

    fun searchPrevious(query: String, caseSensitive: Boolean) {
        webView?.post {
            val escapedQuery = query.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")
            webView?.evaluateJavascript(
                """
                (function() {
                    if (window.PDFViewerApplication && window.PDFViewerApplication.eventBus) {
                        window.PDFViewerApplication.eventBus.dispatch('find', {
                            type: 'again',
                            query: "$escapedQuery",
                            caseSensitive: $caseSensitive,
                            entireWord: false,
                            highlightAll: true,
                            findPrevious: true,
                            matchDiacritics: true
                        });
                    }
                })();
                """.trimIndent(),
                null
            )
        }
    }

    fun clearSearch(caseSensitive: Boolean) {
        webView?.post {
            webView?.evaluateJavascript(
                """
                (function() {
                    if (window.PDFViewerApplication && window.PDFViewerApplication.eventBus) {
                        window.PDFViewerApplication.eventBus.dispatch('find', {
                            type: '',
                            query: '',
                            caseSensitive: $caseSensitive,
                            entireWord: false,
                            highlightAll: false,
                            findPrevious: false,
                            matchDiacritics: true
                        });
                    }
                })();
                """.trimIndent(),
                null
            )
        }
    }

    fun rotatePage() {
        webView?.post {
            webView?.evaluateJavascript(
                """
                (function() {
                    if (window.PDFViewerApplication && window.PDFViewerApplication.pdfViewer) {
                        window.PDFViewerApplication.pdfViewer.pagesRotation += 90;
                    }
                })();
                """.trimIndent(),
                null
            )
        }
    }

    fun toggleSidebar() {
        webView?.post {
            webView?.evaluateJavascript(
                """
                (function() {
                    if (window.PDFViewerApplication && window.PDFViewerApplication.pdfSidebar) {
                        window.PDFViewerApplication.pdfSidebar.toggle();
                    }
                })();
                """.trimIndent(),
                null
            )
        }
    }
}

class AndroidBridge(
    private val onPageChanged: (page: Int, total: Int) -> Unit,
    private val onSearchMatchesCount: (current: Int, total: Int) -> Unit,
    private val onSearchStateChanged: (state: Int, previous: Boolean) -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onPageChanged(page: Int, total: Int) {
        handler.post {
            onPageChanged(page, total)
        }
    }

    @JavascriptInterface
    fun onSearchMatchesCount(current: Int, total: Int) {
        handler.post {
            onSearchMatchesCount(current, total)
        }
    }

    @JavascriptInterface
    fun onSearchStateChanged(state: Int, previous: Boolean) {
        handler.post {
            onSearchStateChanged(state, previous)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    pdf: RecentPdf,
    viewModel: PdfViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentPage by remember { mutableStateOf(1) }
    var totalPages by remember { mutableStateOf(1) }
    val webViewState = remember { PdfWebViewState() }

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchCurrentMatch by remember { mutableStateOf(0) }
    var searchTotalMatches by remember { mutableStateOf(0) }
    var isMatchCase by remember { mutableStateOf(false) }

    val readerFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importLocalPdf(context, it)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (isSearchActive) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Close search button
                        IconButton(
                            onClick = {
                                isSearchActive = false
                                searchQuery = ""
                                searchCurrentMatch = 0
                                searchTotalMatches = 0
                                webViewState.clearSearch(isMatchCase)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "إغلاق البحث",
                                tint = Color.White
                            )
                        }

                        // Search input text field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { q ->
                                searchQuery = q
                                if (q.isEmpty()) {
                                    searchCurrentMatch = 0
                                    searchTotalMatches = 0
                                    webViewState.clearSearch(isMatchCase)
                                } else {
                                    webViewState.performSearch(q, isMatchCase)
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "ابحث عن كلمة...",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White.copy(alpha = 0.5f),
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .padding(vertical = 4.dp),
                            textStyle = TextStyle(fontSize = 14.sp),
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            searchQuery = ""
                                            searchCurrentMatch = 0
                                            searchTotalMatches = 0
                                            webViewState.clearSearch(isMatchCase)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "مسح",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        )

                        // Matches count status
                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = if (searchTotalMatches > 0) {
                                    "$searchCurrentMatch / $searchTotalMatches"
                                } else {
                                    "0/0"
                                },
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        // Case sensitive button "Aa"
                        IconButton(
                            onClick = {
                                val nextMatchCase = !isMatchCase
                                isMatchCase = nextMatchCase
                                if (searchQuery.isNotEmpty()) {
                                    webViewState.performSearch(searchQuery, nextMatchCase)
                                }
                            }
                        ) {
                            Text(
                                text = "Aa",
                                color = if (isMatchCase) Color.White else Color.White.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        // Previous Match button
                        IconButton(
                            onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    webViewState.searchPrevious(searchQuery, isMatchCase)
                                }
                            },
                            enabled = searchQuery.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "السابق",
                                tint = if (searchQuery.isNotEmpty()) Color.White else Color.White.copy(alpha = 0.5f)
                            )
                        }

                        // Next Match button
                        IconButton(
                            onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    webViewState.searchNext(searchQuery, isMatchCase)
                                }
                            },
                            enabled = searchQuery.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "التالي",
                                tint = if (searchQuery.isNotEmpty()) Color.White else Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = pdf.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                isSearchActive = true
                            },
                            modifier = Modifier.testTag("top_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "بحث",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                val file = File(pdf.cachedFilePath)
                                if (file.exists()) {
                                    sharePdf(context, file)
                                } else {
                                    Toast.makeText(context, "الملف غير موجود في الذاكرة المؤقتة لمشاركته", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "مشاركة",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        bottomBar = {
            PdfBottomBar(
                currentPage = currentPage,
                totalPages = totalPages,
                onPrevPage = {
                    webViewState.prevPage()
                },
                onNextPage = {
                    webViewState.nextPage()
                },
                onZoomIn = {
                    webViewState.zoomIn()
                },
                onZoomOut = {
                    webViewState.zoomOut()
                },
                onRotate = {
                    webViewState.rotatePage()
                },
                onToggleSidebar = {
                    webViewState.toggleSidebar()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PdfWebView(
                cachedFilePath = pdf.cachedFilePath,
                state = webViewState,
                onPageChanged = { page, total ->
                    currentPage = page
                    totalPages = total
                },
                onSearchMatchesCount = { current, total ->
                    searchCurrentMatch = current
                    searchTotalMatches = total
                },
                onSearchStateChanged = { state, previous ->
                    if (state == 1) { // FindState.NOT_FOUND
                        searchCurrentMatch = 0
                        searchTotalMatches = 0
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

class PdfJsAssetsPathHandler(private val context: Context) : WebViewAssetLoader.PathHandler {
    override fun handle(path: String): WebResourceResponse? {
        return try {
            val assetPath = "pdfjs/$path"
            val mimeType = getMimeType(path)
            WebResourceResponse(mimeType, "UTF-8", context.assets.open(assetPath))
        } catch (e: Exception) {
            null
        }
    }

    private fun getMimeType(path: String): String {
        return when {
            path.endsWith(".html", ignoreCase = true) -> "text/html"
            path.endsWith(".css", ignoreCase = true) -> "text/css"
            path.endsWith(".js", ignoreCase = true) || path.endsWith(".mjs", ignoreCase = true) -> "application/javascript"
            path.endsWith(".json", ignoreCase = true) -> "application/json"
            path.endsWith(".svg", ignoreCase = true) -> "image/svg+xml"
            path.endsWith(".png", ignoreCase = true) -> "image/png"
            path.endsWith(".gif", ignoreCase = true) -> "image/gif"
            path.endsWith(".ftl", ignoreCase = true) -> "text/plain"
            else -> "application/octet-stream"
        }
    }
}

@Composable
fun PdfWebView(
    cachedFilePath: String,
    state: PdfWebViewState,
    onPageChanged: (page: Int, total: Int) -> Unit,
    onSearchMatchesCount: (current: Int, total: Int) -> Unit,
    onSearchStateChanged: (state: Int, previous: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val assetLoader = remember {
        val pdfCacheDir = File(context.filesDir, "pdf_cache").apply {
            if (!exists()) {
                mkdirs()
            }
        }
        WebViewAssetLoader.Builder()
            .addPathHandler("/pdfjs/", PdfJsAssetsPathHandler(context))
            .addPathHandler("/cache/", WebViewAssetLoader.InternalStoragePathHandler(context, pdfCacheDir))
            .build()
    }

    val cacheFileName = remember(cachedFilePath) {
        File(cachedFilePath).name
    }

    val url = "https://appassets.androidplatform.net/pdfjs/web/viewer.html?file=https://appassets.androidplatform.net/cache/$cacheFileName"

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                state.webView = this

                // Clear cache on startup to ensure updated styles are loaded immediately
                clearCache(true)

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                }

                addJavascriptInterface(
                    AndroidBridge(
                        onPageChanged = onPageChanged,
                        onSearchMatchesCount = onSearchMatchesCount,
                        onSearchStateChanged = onSearchStateChanged
                    ),
                    "AndroidBridge"
                )

                webChromeClient = object : android.webkit.WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                        android.util.Log.d("PDF_JS_CONSOLE", "${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                        return true
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest
                    ): WebResourceResponse? {
                        return assetLoader.shouldInterceptRequest(request.url)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        view?.evaluateJavascript(
                            """
                            (function() {
                                function init() {
                                    console.log("PDF_JS_INIT: Checking PDFViewerApplication");
                                    if (window.PDFViewerApplication && window.PDFViewerApplication.initializedPromise) {
                                        console.log("PDF_JS_INIT: PDFViewerApplication found, injecting styles");
                                        // 0. Inject fail-safe styles directly into the head to hide the top toolbar and find bar
                                        const style = document.createElement("style");
                                        style.textContent = `
                                            .toolbar, #toolbarContainer, .findbar, #findbar, #findbarContainer {
                                                display: none !important;
                                                height: 0px !important;
                                                min-height: 0px !important;
                                                padding: 0 !important;
                                                margin: 0 !important;
                                                overflow: hidden !important;
                                                visibility: hidden !important;
                                            }
                                            #viewerContainer {
                                                top: 0px !important;
                                                bottom: 0px !important;
                                                --visible-toolbar-height: 0px !important;
                                            }
                                        `;
                                        document.head.appendChild(style);

                                        // 2. Register event listeners & fallback polling
                                        window.PDFViewerApplication.initializedPromise.then(() => {
                                            console.log("PDF_JS_INIT: initializedPromise resolved");
                                            const reportPage = (p, t) => {
                                                try {
                                                    console.log("PDF_JS_REPORT: page=" + p + " total=" + t + " hasBridge=" + !!window.AndroidBridge);
                                                    if (window.AndroidBridge && typeof window.AndroidBridge.onPageChanged === "function") {
                                                        window.AndroidBridge.onPageChanged(p, t);
                                                    } else {
                                                        console.warn("PDF_JS_REPORT_WARN: AndroidBridge not found or onPageChanged is not a function");
                                                    }
                                                } catch (e) {
                                                    console.error("PDF_JS_REPORT_ERROR: reportPage failed: " + e.message);
                                                }
                                            };

                                            // Subscribe to events
                                            try {
                                                if (window.PDFViewerApplication.eventBus) {
                                                    window.PDFViewerApplication.eventBus.on('pagechanging', (e) => {
                                                        const total = (window.PDFViewerApplication.pdfDocument ? window.PDFViewerApplication.pdfDocument.numPages : 0) || window.PDFViewerApplication.pagesCount || 1;
                                                        reportPage(e.pageNumber, total);
                                                    });

                                                    window.PDFViewerApplication.eventBus.on('pagesinit', () => {
                                                        const p = (window.PDFViewerApplication.pdfViewer && window.PDFViewerApplication.pdfViewer.currentPageNumber) || 1;
                                                        const total = (window.PDFViewerApplication.pdfDocument ? window.PDFViewerApplication.pdfDocument.numPages : 0) || window.PDFViewerApplication.pagesCount || 1;
                                                        reportPage(p, total);
                                                    });

                                                    window.PDFViewerApplication.eventBus.on('updatefindmatchescount', (e) => {
                                                        try {
                                                            if (window.AndroidBridge && typeof window.AndroidBridge.onSearchMatchesCount === 'function') {
                                                                window.AndroidBridge.onSearchMatchesCount(e.matchesCount.current, e.matchesCount.total);
                                                            }
                                                        } catch (err) {
                                                            console.error("PDF_JS_FIND_ERROR: updatefindmatchescount: " + err.message);
                                                        }
                                                    });

                                                    window.PDFViewerApplication.eventBus.on('updatefindcontrolstate', (e) => {
                                                        try {
                                                            if (window.AndroidBridge && typeof window.AndroidBridge.onSearchStateChanged === 'function') {
                                                                window.AndroidBridge.onSearchStateChanged(e.state, e.previous);
                                                            }
                                                        } catch (err) {
                                                            console.error("PDF_JS_FIND_ERROR: updatefindcontrolstate: " + err.message);
                                                        }
                                                    });

                                                    console.log("PDF_JS_INIT: Event listeners registered");
                                                }
                                            } catch (e) {
                                                console.error("PDF_JS_INIT_ERROR: Event subscription failed: " + e.message);
                                            }

                                            // 3. Fallback continuous polling to guarantee instantaneous updates
                                            let lastPage = -1;
                                            let lastTotal = -1;
                                            function poll() {
                                                try {
                                                    if (window.PDFViewerApplication) {
                                                        let page = 1;
                                                        if (window.PDFViewerApplication.pdfViewer) {
                                                            page = window.PDFViewerApplication.pdfViewer.currentPageNumber || 1;
                                                        } else if (window.PDFViewerApplication.page) {
                                                            page = window.PDFViewerApplication.page;
                                                        }

                                                        let total = 0;
                                                        if (window.PDFViewerApplication.pdfDocument) {
                                                            total = window.PDFViewerApplication.pdfDocument.numPages || 0;
                                                        } else if (window.PDFViewerApplication.pagesCount) {
                                                            total = window.PDFViewerApplication.pagesCount;
                                                        }

                                                        if (page !== lastPage || total !== lastTotal) {
                                                            lastPage = page;
                                                            lastTotal = total;
                                                            reportPage(page, total);
                                                        }
                                                    }
                                                } catch (e) {
                                                    console.error("PDF_JS_POLL_ERROR: polling failed: " + e.message);
                                                }
                                                setTimeout(poll, 250);
                                            }
                                            poll();
                                        }).catch(e => {
                                            console.error("PDF_JS_INIT_ERROR: initializedPromise then failed: " + e.message);
                                        });
                                    } else {
                                        setTimeout(init, 50);
                                    }
                                }
                                init();
                            })();
                            """.trimIndent(),
                            null
                        )
                    }

                    override fun onReceivedError(
                        view: WebView,
                        request: WebResourceRequest,
                        error: WebResourceError
                    ) {
                        super.onReceivedError(view, request, error)
                    }
                }

                loadUrl(url)
            }
        },
        modifier = modifier
    )
}

fun sharePdf(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "com.aistudio.pdfviewer.xqkzn.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة مستند PDF"))
    } catch (e: Exception) {
        Toast.makeText(context, "فشل مشاركة الملف: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
