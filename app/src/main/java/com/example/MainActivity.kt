package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.draw.drawWithContent
import kotlin.math.roundToInt
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

// Imports for custom bottom sheets and widgets
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Slider
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.WindowManager
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
    onToggleSidebar: () -> Unit,
    onZoomClick: () -> Unit,
    onDisplaySettingsClick: () -> Unit,
    onMoreOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Pages/Sidebar Button (البيجات)
            IconButton(
                onClick = onToggleSidebar,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("bottom_pages")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ViewSidebar,
                    contentDescription = "الصفحات الجانبية",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 2. Zoom Button (الزوم وعرض المحتوى)
            IconButton(
                onClick = onZoomClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("bottom_zoom")
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "الزوم وعرض المحتوى",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 3. Display Settings Button (إعدادات العرض)
            IconButton(
                onClick = onDisplaySettingsClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("bottom_display_settings")
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "إعدادات العرض والتنسيق",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 4. More Options Button (٣ نقاط)
            IconButton(
                onClick = onMoreOptionsClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("bottom_more_options")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "المزيد من الخيارات والوظائف",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

class PdfWebViewState {
    var webView: WebView? = null

    // --- NEW FUNCTIONS FOR ADVANCED POPUPS ---
    fun zoomActualSize() {
        webView?.post {
            webView?.evaluateJavascript(
                """
                (function() {
                    if (window.PDFViewerApplication && window.PDFViewerApplication.pdfViewer) {
                        window.PDFViewerApplication.pdfViewer.currentScale = 1.0;
                    }
                })();
                """.trimIndent(),
                null
            )
        }
    }

    fun zoomFitWidth() {
        webView?.post {
            webView?.evaluateJavascript(
                """
                (function() {
                    if (window.PDFViewerApplication && window.PDFViewerApplication.pdfViewer) {
                        window.PDFViewerApplication.pdfViewer.currentScaleValue = 'page-width';
                    }
                })();
                """.trimIndent(),
                null
            )
        }
    }

    fun zoomFitPage() {
        webView?.post {
            webView?.evaluateJavascript(
                """
                (function() {
                    if (window.PDFViewerApplication && window.PDFViewerApplication.pdfViewer) {
                        window.PDFViewerApplication.pdfViewer.currentScaleValue = 'page-fit';
                    }
                })();
                """.trimIndent(),
                null
            )
        }
    }

    fun setReadingTheme(theme: String) {
        webView?.post {
            val css = when (theme) {
                "dark" -> """
                    body, #viewerContainer { background-color: #1E1F22 !important; }
                    .page { background-color: #2B2D31 !important; filter: invert(0.9) hue-rotate(180deg) !important; }
                """.trimIndent()
                "black" -> """
                    body, #viewerContainer { background-color: #000000 !important; }
                    .page { background-color: #111111 !important; filter: invert(1) hue-rotate(180deg) !important; }
                """.trimIndent()
                "sepia" -> """
                    body, #viewerContainer { background-color: #F4ECD8 !important; }
                    .page { background-color: #FCF5E3 !important; filter: sepia(0.6) contrast(0.95) !important; }
                """.trimIndent()
                else -> """
                    body, #viewerContainer { background-color: #F4F4F4 !important; }
                    .page { background-color: #FFFFFF !important; filter: none !important; }
                """.trimIndent()
            }
            val js = """
                (function() {
                    const styleId = 'pdf-custom-theme-style';
                    let style = document.getElementById(styleId);
                    if (!style) {
                        style = document.createElement('style');
                        style.id = styleId;
                        document.head.appendChild(style);
                    }
                    style.innerHTML = `${css.replace("\n", " ")}`;
                })();
            """.trimIndent()
            webView?.evaluateJavascript(js, null)
        }
    }

    fun setScrollMode(mode: Int) {
        webView?.post {
            webView?.evaluateJavascript(
                """
                (function() {
                    if (window.PDFViewerApplication && window.PDFViewerApplication.pdfViewer) {
                        window.PDFViewerApplication.pdfViewer.scrollMode = $mode;
                    }
                })();
                """.trimIndent(),
                null
            )
        }
    }

    fun setPageSnapping(enabled: Boolean) {
        webView?.post {
            val js = if (enabled) {
                """
                (function() {
                    const el = document.getElementById('viewerContainer');
                    if (el) {
                        el.style.scrollSnapType = 'y mandatory';
                        const pages = document.getElementsByClassName('page');
                        for (let page of pages) {
                            page.style.scrollSnapAlign = 'start';
                        }
                    }
                })();
                """.trimIndent()
            } else {
                """
                (function() {
                    const el = document.getElementById('viewerContainer');
                    if (el) {
                        el.style.scrollSnapType = 'none';
                    }
                })();
                """.trimIndent()
            }
            webView?.evaluateJavascript(js, null)
        }
    }

    fun jumpToPage(page: Int) {
        webView?.post {
            webView?.evaluateJavascript(
                """
                (function() {
                    if (window.PDFViewerApplication && window.PDFViewerApplication.pdfViewer) {
                        window.PDFViewerApplication.pdfViewer.currentPageNumber = $page;
                    }
                })();
                """.trimIndent(),
                null
            )
        }
    }

    fun startAutoScroll(speedMs: Int) {
        webView?.post {
            webView?.evaluateJavascript(
                """
                (function() {
                    if (window.pdfAutoScrollInterval) {
                        clearInterval(window.pdfAutoScrollInterval);
                    }
                    const el = document.getElementById('viewerContainer');
                    window.pdfAutoScrollInterval = setInterval(function() {
                        if (el) {
                            el.scrollTop += 1;
                        }
                    }, $speedMs);
                })();
                """.trimIndent(),
                null
            )
        }
    }

    fun stopAutoScroll() {
        webView?.post {
            webView?.evaluateJavascript(
                """
                (function() {
                    if (window.pdfAutoScrollInterval) {
                        clearInterval(window.pdfAutoScrollInterval);
                        window.pdfAutoScrollInterval = null;
                    }
                })();
                """.trimIndent(),
                null
            )
        }
    }

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
        android.util.Log.d("PDF_BRIDGE", "onPageChanged called: page=$page, total=$total")
        handler.post {
            onPageChanged(page, total)
        }
    }

    @JavascriptInterface
    fun onSearchMatchesCount(current: Int, total: Int) {
        android.util.Log.d("PDF_BRIDGE", "onSearchMatchesCount called: current=$current, total=$total")
        handler.post {
            onSearchMatchesCount(current, total)
        }
    }

    @JavascriptInterface
    fun onSearchStateChanged(state: Int, previous: Boolean) {
        android.util.Log.d("PDF_BRIDGE", "onSearchStateChanged called: state=$state, previous=$previous")
        handler.post {
            onSearchStateChanged(state, previous)
        }
    }
}

enum class ReaderBottomSheetType {
    ZOOM_DISPLAY,
    DISPLAY_SETTINGS,
    MORE_OPTIONS,
    BOOKMARKS_LIST,
    JUMP_PAGE,
    DOCUMENT_INFO
}

@Composable
fun ZoomDisplayBottomSheet(
    zoomPercentage: Int,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onActualSize: () -> Unit,
    onFitWidth: () -> Unit,
    onFitPage: () -> Unit,
    currentOrientation: Int,
    onOrientationChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "الزوم وعرض المحتوى",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

        // Zoom Control Row
        Text(
            text = "نسبة التكبير والتصغير",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onZoomOut,
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(Icons.Default.ZoomOut, contentDescription = "تصغير")
            }

            Text(
                text = "$zoomPercentage%",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(
                onClick = onZoomIn,
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(Icons.Default.ZoomIn, contentDescription = "تكبير")
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Quick Zoom Row
        Text(
            text = "الخيارات السريعة",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val presets = listOf(
                "الحجم الفعلي" to onActualSize,
                "عرض الصفحة" to onFitWidth,
                "ملائمة الصفحة" to onFitPage
            )
            presets.forEach { (label, onClick) ->
                Button(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Orientation Control Row
        Text(
            text = "اتجاه الشاشة",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val orientations = listOf(
                Triple("تلقائي", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, Icons.Default.ScreenRotation),
                Triple("أفقي", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, Icons.Default.ScreenRotation),
                Triple("رأسي", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, Icons.Default.ScreenRotation)
            )
            orientations.forEach { (label, value, icon) ->
                val isSelected = currentOrientation == value
                Button(
                    onClick = { onOrientationChange(value) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DisplaySettingsBottomSheet(
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    useSystemBrightness: Boolean,
    onUseSystemBrightnessChange: (Boolean) -> Unit,
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    keepScreenOn: Boolean,
    onKeepScreenOnChange: (Boolean) -> Unit,
    scrollMode: Int,
    onScrollModeChange: (Int) -> Unit,
    snapToPage: Boolean,
    onSnapToPageChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "إعدادات العرض والتنسيق",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

        // Themes
        Text(text = "سمة القراءة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val themes = listOf(
                "light" to "فاتح",
                "dark" to "داكن",
                "black" to "أسود",
                "sepia" to "سيبيا"
            )
            themes.forEach { (key, label) ->
                val isSelected = currentTheme == key
                val (bgColor, textColor) = when (key) {
                    "light" -> Color.White to Color.Black
                    "dark" -> Color(0xFF1E1F22) to Color.White
                    "black" -> Color.Black to Color.White
                    "sepia" -> Color(0xFFF4ECD8) to Color(0xFF5B4636)
                    else -> Color.White to Color.Black
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(bgColor, RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onThemeChange(key) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))

        // Brightness
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "سطوع الشاشة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = useSystemBrightness,
                    onCheckedChange = onUseSystemBrightnessChange
                )
                Text(text = "تلقائي", style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (!useSystemBrightness) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Brightness4, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(
                    value = brightness,
                    onValueChange = onBrightnessChange,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))

        // Keep screen on
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "إبقاء الشاشة مضاءة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(text = "منع إيقاف تشغيل الشاشة أثناء القراءة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = keepScreenOn,
                onCheckedChange = onKeepScreenOnChange
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))

        // Scroll Mode
        Text(text = "اتجاه التصفح", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(0 to "رأسي متتالي", 1 to "أفقي صفحة بصفحة").forEach { (mode, label) ->
                val isSelected = scrollMode == mode
                Button(
                    onClick = { onScrollModeChange(mode) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))

        // Page Snapping
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "محاذاة الصفحات التلقائي (Snap)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(text = "محاذاة حواف الصفحات أثناء التمرير", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = snapToPage,
                onCheckedChange = onSnapToPageChange
            )
        }
    }
}

@Composable
fun MoreOptionsBottomSheet(
    currentPage: Int,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onViewBookmarks: () -> Unit,
    isAutoScrolling: Boolean,
    onToggleAutoScroll: () -> Unit,
    autoScrollSpeedMs: Int,
    onSpeedChange: (Int) -> Unit,
    onJumpToPage: () -> Unit,
    onShare: () -> Unit,
    onPrint: () -> Unit,
    onDocInfo: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "خيارات وأدوات إضافية",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        }

        // Section: Reading tools
        item {
            Text(text = "أدوات القراءة", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }

        // Bookmark toggle
        item {
            MoreOptionItem(
                icon = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                title = if (isBookmarked) "إزالة الصفحة $currentPage من الإشارات المرجعية" else "إضافة الصفحة $currentPage للإشارات المرجعية",
                onClick = onToggleBookmark,
                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }

        // View bookmarks
        item {
            MoreOptionItem(
                icon = Icons.Default.Bookmark,
                title = "عرض كل الإشارات المرجعية المحفوظة",
                onClick = onViewBookmarks
            )
        }

        // Auto-scroll control
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                MoreOptionItem(
                    icon = if (isAutoScrolling) Icons.Default.Stop else Icons.Default.PlayArrow,
                    title = if (isAutoScrolling) "إيقاف التمرير التلقائي" else "تشغيل التمرير التلقائي",
                    onClick = onToggleAutoScroll,
                    tint = if (isAutoScrolling) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )

                if (isAutoScrolling) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "سرعة التمرير:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val speeds = listOf(
                            120 to "بطيء",
                            60 to "متوسط",
                            30 to "سريع",
                            15 to "سريع جداً"
                        )
                        speeds.forEach { (ms, label) ->
                            val isSelected = autoScrollSpeedMs == ms
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSpeedChange(ms) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Jump to page
        item {
            MoreOptionItem(
                icon = Icons.Default.Settings,
                title = "الذهاب المباشر إلى صفحة محددة",
                onClick = onJumpToPage
            )
        }

        // Section: Document actions
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
            Text(text = "خيارات الملف ومشاركته", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }

        // Share document
        item {
            MoreOptionItem(
                icon = Icons.Default.Share,
                title = "مشاركة هذا المستند مع الآخرين",
                onClick = onShare
            )
        }

        // Print document
        item {
            MoreOptionItem(
                icon = Icons.Default.Print,
                title = "طباعة المستند الحالي",
                onClick = onPrint
            )
        }

        // Document Info
        item {
            MoreOptionItem(
                icon = Icons.Default.Info,
                title = "معلومات وبيانات المستند التفصيلية",
                onClick = onDocInfo
            )
        }
    }
}

@Composable
fun MoreOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = tint)
    }
}

@Composable
fun BookmarksListBottomSheet(
    bookmarks: Set<Int>,
    onBookmarkClick: (Int) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "العلامات المرجعية المحفوظة",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

        if (bookmarks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "لا توجد علامات مرجعية محفوظة بعد.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "يمكنك إضافة الصفحة الحالية من خلال خيارات الثلاث نقاط.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(bookmarks.toList().sorted()) { pageNum ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .clickable {
                                onBookmarkClick(pageNum)
                                onClose()
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "صفحة $pageNum",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "الانتقال",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentInfoBottomSheet(
    pdf: RecentPdf,
    totalPages: Int,
    currentPage: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "معلومات المستند",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoBox(
                label = "نوع الملف",
                value = "PDF",
                modifier = Modifier.weight(1f)
            )

            InfoBox(
                label = "حجم الملف",
                value = formatFileSize(pdf.fileSize),
                modifier = Modifier.weight(1f)
            )

            InfoBox(
                label = "الصفحات",
                value = "$totalPages",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        DetailRow(label = "عنوان المستند", value = pdf.title)
        DetailRow(label = "مسار الملف الكاش", value = pdf.cachedFilePath)
        DetailRow(label = "المصدر الأصلي", value = getReadablePath(pdf.sourceUriOrUrl))
        DetailRow(label = "آخر صفحة مقروءة", value = "الصفحة $currentPage")
        
        val dateFormater = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
        DetailRow(label = "تاريخ الاستيراد", value = dateFormater.format(Date(pdf.timestamp)))
    }
}

private fun getReadablePath(uriString: String): String {
    return try {
        val decoded = Uri.decode(uriString)
        if (decoded.startsWith("content://")) {
            if (decoded.contains("raw:")) {
                decoded.substringAfter("raw:")
            } else if (decoded.contains("/document/primary:")) {
                "/storage/emulated/0/" + decoded.substringAfter("/document/primary:")
            } else if (decoded.contains("/document/")) {
                val docId = decoded.substringAfter("/document/")
                if (docId.startsWith("msf:") || docId.startsWith("raw:")) {
                    docId.substringAfter("raw:")
                } else {
                    decoded
                }
            } else {
                decoded
            }
        } else {
            decoded
        }
    } catch (e: Exception) {
        uriString
    }
}

@Composable
fun InfoBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = label, 
                style = MaterialTheme.typography.bodyMedium, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(0.35f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(0.65f)
                    .padding(start = 16.dp)
                    .clickable {
                        try {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText(label, value)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ $label بنجاح", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            // Ignore clipboard errors
                        }
                    },
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
    }
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

@Composable
fun JumpPageBottomSheet(
    currentPage: Int,
    totalPages: Int,
    onJump: (Int) -> Unit,
    onClose: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "الذهاب إلى صفحة",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

        Text(
            text = "أدخل رقم الصفحة بين 1 و $totalPages (أنت حالياً في الصفحة $currentPage)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = textInput,
            onValueChange = {
                textInput = it
                errorMsg = null
            },
            label = { Text("رقم الصفحة") },
            placeholder = { Text("مثال: 45") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(
                onGo = {
                    val page = textInput.toIntOrNull()
                    if (page != null && page in 1..totalPages) {
                        onJump(page)
                        onClose()
                    } else {
                        errorMsg = "الرجاء إدخال رقم صفحة صحيح بين 1 و $totalPages"
                    }
                }
            ),
            modifier = Modifier.fillMaxWidth(),
            isError = errorMsg != null
        )

        if (errorMsg != null) {
            Text(
                text = errorMsg!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    val page = textInput.toIntOrNull()
                    if (page != null && page in 1..totalPages) {
                        onJump(page)
                        onClose()
                    } else {
                        errorMsg = "الرجاء إدخال رقم صفحة صحيح بين 1 و $totalPages"
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ذهاب", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onClose,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("إلغاء", fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun printPdf(context: Context, pdf: RecentPdf) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? android.print.PrintManager ?: return
    val jobName = "طباعة - ${pdf.title}"
    val file = File(pdf.cachedFilePath)
    if (!file.exists()) return
    
    val printAdapter = object : android.print.PrintDocumentAdapter() {
        override fun onLayout(
            oldAttributes: android.print.PrintAttributes?,
            newAttributes: android.print.PrintAttributes?,
            cancellationSignal: android.os.CancellationSignal?,
            callback: LayoutResultCallback?,
            extras: android.os.Bundle?
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback?.onLayoutCancelled()
                return
            }
            val info = android.print.PrintDocumentInfo.Builder(pdf.title)
                .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .build()
            callback?.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out android.print.PageRange>?,
            destination: android.os.ParcelFileDescriptor?,
            cancellationSignal: android.os.CancellationSignal?,
            callback: WriteResultCallback?
        ) {
            var input: java.io.InputStream? = null
            var output: java.io.OutputStream? = null
            try {
                input = file.inputStream()
                output = java.io.FileOutputStream(destination?.fileDescriptor)
                input.copyTo(output)
                callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
            } catch (e: Exception) {
                callback?.onWriteFailed(e.toString())
            } finally {
                input?.close()
                output?.close()
            }
        }
    }
    printManager.print(jobName, printAdapter, null)
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
    var totalPages by remember(pdf) { mutableStateOf(getPdfPageCount(pdf.cachedFilePath)) }
    val webViewState = remember { PdfWebViewState() }

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchCurrentMatch by remember { mutableStateOf(0) }
    var searchTotalMatches by remember { mutableStateOf(0) }
    var isMatchCase by remember { mutableStateOf(false) }

    // Custom Bottom Sheets States
    var activeBottomSheet by remember { mutableStateOf<ReaderBottomSheetType?>(null) }
    var zoomPercentage by remember { mutableStateOf(100) }
    var screenOrientation by remember { mutableStateOf(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) }
    var readingTheme by remember { mutableStateOf("light") }
    var useSystemBrightness by remember { mutableStateOf(true) }
    var brightnessValue by remember { mutableStateOf(0.5f) }
    var keepScreenOn by remember { mutableStateOf(false) }
    var scrollMode by remember { mutableStateOf(0) } // 0: Vertical, 1: Horizontal
    var snapToPage by remember { mutableStateOf(false) }

    // Bookmarking and Auto-scroll
    var bookmarks by remember { mutableStateOf(setOf<Int>()) }
    var isAutoScrolling by remember { mutableStateOf(false) }
    var autoScrollSpeedMs by remember { mutableStateOf(60) }

    // Bookmarks persistence
    val prefs = remember { context.getSharedPreferences("pdf_reader_prefs", Context.MODE_PRIVATE) }
    val pdfKey = remember(pdf) { "bookmarks_${pdf.title.hashCode()}" }
    LaunchedEffect(pdf) {
        val saved = prefs.getStringSet(pdfKey, emptySet()) ?: emptySet()
        bookmarks = saved.mapNotNull { it.toIntOrNull() }.toSet()
    }

    // Keep screen on side effect
    val activity = context as? Activity
    DisposableEffect(keepScreenOn) {
        if (keepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Reading theme side effect
    LaunchedEffect(readingTheme) {
        webViewState.setReadingTheme(readingTheme)
    }

    // Scroll mode side effect
    LaunchedEffect(scrollMode) {
        webViewState.setScrollMode(scrollMode)
    }

    // Snap to page side effect
    LaunchedEffect(snapToPage) {
        webViewState.setPageSnapping(snapToPage)
    }

    // Auto-scroll side effect
    LaunchedEffect(isAutoScrolling, autoScrollSpeedMs) {
        if (isAutoScrolling) {
            webViewState.startAutoScroll(autoScrollSpeedMs)
        } else {
            webViewState.stopAutoScroll()
        }
    }

    // Screen brightness side effect
    LaunchedEffect(useSystemBrightness, brightnessValue) {
        activity?.let { act ->
            val lp = act.window.attributes
            if (useSystemBrightness) {
                lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            } else {
                lp.screenBrightness = brightnessValue.coerceIn(0.01f, 1.0f)
            }
            act.window.attributes = lp
        }
    }

    // Restore screen brightness on dispose
    DisposableEffect(Unit) {
        onDispose {
            activity?.let { act ->
                val lp = act.window.attributes
                lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                act.window.attributes = lp
                // Restore orientation to normal when leaving
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    val readerFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importLocalPdf(context, it)
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {}
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // The WebView spans the whole area
            PdfWebView(
                cachedFilePath = pdf.cachedFilePath,
                state = webViewState,
                readingTheme = readingTheme,
                onPageChanged = { page, total ->
                    currentPage = page
                    if (total > 1) {
                        totalPages = total
                    }
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
                modifier = Modifier.fillMaxSize()
            )

            // 1. Floating Capsule Top Bar
            val capsuleColors = getCapsuleColors(readingTheme)
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (isSearchActive) {
                    SearchCapsuleBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { q ->
                            searchQuery = q
                            if (q.isEmpty()) {
                                searchCurrentMatch = 0
                                searchTotalMatches = 0
                                webViewState.clearSearch(isMatchCase)
                            } else {
                                webViewState.performSearch(q, isMatchCase)
                            }
                        },
                        searchCurrentMatch = searchCurrentMatch,
                        searchTotalMatches = searchTotalMatches,
                        isMatchCase = isMatchCase,
                        onMatchCaseChange = { isMatchCase = it },
                        onPrevMatch = { webViewState.searchPrevious(searchQuery, isMatchCase) },
                        onNextMatch = { webViewState.searchNext(searchQuery, isMatchCase) },
                        onCloseSearch = {
                            isSearchActive = false
                            searchQuery = ""
                            searchCurrentMatch = 0
                            searchTotalMatches = 0
                            webViewState.clearSearch(isMatchCase)
                        },
                        colors = capsuleColors
                    )
                } else {
                    NormalCapsuleBar(
                        pdfTitle = pdf.title,
                        onBack = onBack,
                        onSearchClick = { isSearchActive = true },
                        onShareClick = {
                            val file = File(pdf.cachedFilePath)
                            if (file.exists()) {
                                sharePdf(context, file)
                            } else {
                                Toast.makeText(context, "الملف غير موجود في الذاكرة المؤقتة لمشاركته", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = capsuleColors
                    )
                }
            }

            // 2. Custom Vertical Fast Scroller with Teardrop
            PdfVerticalScroller(
                currentPage = currentPage,
                totalPages = totalPages,
                onPageChange = { page ->
                    currentPage = page
                    webViewState.jumpToPage(page)
                },
                readingTheme = readingTheme,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
            )

            // 3. Floating Capsule Bottom Bar (Bottom Center)
            PdfBottomBar(
                onToggleSidebar = { webViewState.toggleSidebar() },
                onZoomClick = { activeBottomSheet = ReaderBottomSheetType.ZOOM_DISPLAY },
                onDisplaySettingsClick = { activeBottomSheet = ReaderBottomSheetType.DISPLAY_SETTINGS },
                onMoreOptionsClick = { activeBottomSheet = ReaderBottomSheetType.MORE_OPTIONS },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .navigationBarsPadding()
            )
        }
    }

    if (activeBottomSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { activeBottomSheet = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 16.dp
        ) {
            when (activeBottomSheet) {
                ReaderBottomSheetType.ZOOM_DISPLAY -> {
                    ZoomDisplayBottomSheet(
                        zoomPercentage = zoomPercentage,
                        onZoomIn = {
                            webViewState.zoomIn()
                            zoomPercentage = (zoomPercentage + 25).coerceAtMost(300)
                        },
                        onZoomOut = {
                            webViewState.zoomOut()
                            zoomPercentage = (zoomPercentage - 25).coerceAtLeast(25)
                        },
                        onActualSize = {
                            webViewState.zoomActualSize()
                            zoomPercentage = 100
                        },
                        onFitWidth = {
                            webViewState.zoomFitWidth()
                        },
                        onFitPage = {
                            webViewState.zoomFitPage()
                        },
                        currentOrientation = screenOrientation,
                        onOrientationChange = { orient ->
                            screenOrientation = orient
                            (context as? Activity)?.requestedOrientation = orient
                        }
                    )
                }
                ReaderBottomSheetType.DISPLAY_SETTINGS -> {
                    DisplaySettingsBottomSheet(
                        currentTheme = readingTheme,
                        onThemeChange = { readingTheme = it },
                        useSystemBrightness = useSystemBrightness,
                        onUseSystemBrightnessChange = { useSystemBrightness = it },
                        brightness = brightnessValue,
                        onBrightnessChange = { brightnessValue = it },
                        keepScreenOn = keepScreenOn,
                        onKeepScreenOnChange = { keepScreenOn = it },
                        scrollMode = scrollMode,
                        onScrollModeChange = { scrollMode = it },
                        snapToPage = snapToPage,
                        onSnapToPageChange = { snapToPage = it }
                    )
                }
                ReaderBottomSheetType.MORE_OPTIONS -> {
                    MoreOptionsBottomSheet(
                        currentPage = currentPage,
                        isBookmarked = bookmarks.contains(currentPage),
                        onToggleBookmark = {
                            val newBookmarks = bookmarks.toMutableSet()
                            if (newBookmarks.contains(currentPage)) {
                                newBookmarks.remove(currentPage)
                            } else {
                                newBookmarks.add(currentPage)
                            }
                            bookmarks = newBookmarks
                            prefs.edit().putStringSet(pdfKey, newBookmarks.map { it.toString() }.toSet()).apply()
                        },
                        onViewBookmarks = {
                            activeBottomSheet = ReaderBottomSheetType.BOOKMARKS_LIST
                        },
                        isAutoScrolling = isAutoScrolling,
                        onToggleAutoScroll = {
                            isAutoScrolling = !isAutoScrolling
                        },
                        autoScrollSpeedMs = autoScrollSpeedMs,
                        onSpeedChange = { autoScrollSpeedMs = it },
                        onJumpToPage = {
                            activeBottomSheet = ReaderBottomSheetType.JUMP_PAGE
                        },
                        onShare = {
                            val file = File(pdf.cachedFilePath)
                            if (file.exists()) {
                                sharePdf(context, file)
                            } else {
                                Toast.makeText(context, "الملف غير موجود لمشاركته", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onPrint = {
                            printPdf(context, pdf)
                        },
                        onDocInfo = {
                            activeBottomSheet = ReaderBottomSheetType.DOCUMENT_INFO
                        }
                    )
                }
                ReaderBottomSheetType.BOOKMARKS_LIST -> {
                    BookmarksListBottomSheet(
                        bookmarks = bookmarks,
                        onBookmarkClick = { page ->
                            webViewState.jumpToPage(page)
                        },
                        onClose = { activeBottomSheet = null }
                    )
                }
                ReaderBottomSheetType.JUMP_PAGE -> {
                    JumpPageBottomSheet(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onJump = { page ->
                            webViewState.jumpToPage(page)
                        },
                        onClose = { activeBottomSheet = null }
                    )
                }
                ReaderBottomSheetType.DOCUMENT_INFO -> {
                    DocumentInfoBottomSheet(
                        pdf = pdf,
                        totalPages = totalPages,
                        currentPage = currentPage
                    )
                }
                else -> {}
            }
        }
    }
}

// --- CUSTOM MODERN COMPONENTS FOR ADVANCED CAPSULE TOP BAR AND TEARDROP SCROLLER ---

data class CapsuleColors(
    val backgroundColor: Color,
    val contentColor: Color,
    val borderColor: Color
)

@Composable
fun getCapsuleColors(readingTheme: String): CapsuleColors {
    return when (readingTheme) {
        "dark" -> CapsuleColors(
            backgroundColor = Color(0xFF2B2D31).copy(alpha = 0.95f),
            contentColor = Color(0xFFF4F6F6),
            borderColor = Color.White.copy(alpha = 0.08f)
        )
        "black" -> CapsuleColors(
            backgroundColor = Color(0xFF1E1F22).copy(alpha = 0.95f),
            contentColor = Color(0xFFE3E3E3),
            borderColor = Color.White.copy(alpha = 0.05f)
        )
        "sepia" -> CapsuleColors(
            backgroundColor = Color(0xFFF4ECD8).copy(alpha = 0.95f),
            contentColor = Color(0xFF4E3629),
            borderColor = Color(0xFF4E3629).copy(alpha = 0.1f)
        )
        else -> CapsuleColors(
            backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    }
}

@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    maxLines: Int = 1
) {
    var fontSize by remember(text) { mutableStateOf(16.sp) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        color = color,
        fontWeight = fontWeight,
        fontSize = fontSize,
        maxLines = maxLines,
        overflow = TextOverflow.Clip,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow) {
                if (fontSize.value > 10f) {
                    fontSize = (fontSize.value - 1f).sp
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        }
    )
}

@Composable
fun NormalCapsuleBar(
    pdfTitle: String,
    onBack: () -> Unit,
    onSearchClick: () -> Unit,
    onShareClick: () -> Unit,
    colors: CapsuleColors,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        shape = CircleShape,
        color = colors.backgroundColor,
        border = BorderStroke(1.dp, colors.borderColor),
        shadowElevation = 6.dp,
        tonalElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = colors.contentColor
                )
            }

            AutoSizeText(
                text = pdfTitle,
                color = colors.contentColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.testTag("top_search_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "بحث",
                    tint = colors.contentColor
                )
            }

            IconButton(
                onClick = onShareClick,
                modifier = Modifier.testTag("share_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "مشاركة",
                    tint = colors.contentColor
                )
            }
        }
    }
}

@Composable
fun SearchCapsuleBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchCurrentMatch: Int,
    searchTotalMatches: Int,
    isMatchCase: Boolean,
    onMatchCaseChange: (Boolean) -> Unit,
    onPrevMatch: () -> Unit,
    onNextMatch: () -> Unit,
    onCloseSearch: () -> Unit,
    colors: CapsuleColors,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = colors.backgroundColor,
        border = BorderStroke(1.dp, colors.borderColor),
        shadowElevation = 6.dp,
        tonalElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCloseSearch,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "إغلاق البحث",
                    tint = colors.contentColor
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = colors.contentColor,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(colors.contentColor),
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .background(colors.contentColor.copy(alpha = 0.1f), CircleShape)
                    .padding(horizontal = 12.dp),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "البحث عن كلمة...",
                                    color = colors.contentColor.copy(alpha = 0.5f),
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "مسح",
                                    tint = colors.contentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(4.dp))

            if (searchQuery.isNotEmpty()) {
                Text(
                    text = if (searchTotalMatches > 0) {
                        "$searchCurrentMatch / $searchTotalMatches"
                    } else {
                        "0/0"
                    },
                    color = colors.contentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            IconButton(
                onClick = { onMatchCaseChange(!isMatchCase) },
                modifier = Modifier.size(40.dp)
            ) {
                Text(
                    text = "Aa",
                    color = if (isMatchCase) colors.contentColor else colors.contentColor.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            IconButton(
                onClick = onPrevMatch,
                enabled = searchQuery.isNotEmpty(),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "السابق",
                    tint = if (searchQuery.isNotEmpty()) colors.contentColor else colors.contentColor.copy(alpha = 0.3f)
                )
            }

            IconButton(
                onClick = onNextMatch,
                enabled = searchQuery.isNotEmpty(),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "التالي",
                    tint = if (searchQuery.isNotEmpty()) colors.contentColor else colors.contentColor.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun PdfVerticalScroller(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    readingTheme: String,
    modifier: Modifier = Modifier
) {
    if (totalPages <= 1) return

    // Track state of dragging
    var isDragging by remember { mutableStateOf(false) }
    // Local drag position y (fraction from 0.0 to 1.0)
    var dragFraction by remember { mutableStateOf(0f) }

    // Use a spring animation to smoothly transition the handle position when NOT dragging
    val targetFraction = ((currentPage - 1).toFloat() / (totalPages - 1).toFloat()).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "ScrollFraction"
    )

    val currentFraction = if (isDragging) dragFraction else animatedFraction

    // Determine colors based on reading theme
    val capsuleColors = getCapsuleColors(readingTheme)
    // Custom beautiful purple for the scroll handle to match user's image, or theme color
    val accentPurple = Color(0xFF9C27B0)
    val activeTrackColor = accentPurple.copy(alpha = 0.8f)
    val inactiveTrackColor = capsuleColors.contentColor.copy(alpha = 0.15f)

    BoxWithConstraints(
        modifier = modifier
            .width(100.dp)
            .fillMaxHeight()
            .padding(vertical = 120.dp) // Avoid overlapping with top capsule and bottom floating bar
    ) {
        val totalHeightPx = constraints.maxHeight.toFloat()
        if (totalHeightPx <= 0f) return@BoxWithConstraints

        // Vertical Track (on the right side)
        // We place the track at x = 84.dp (which is 16.dp from the right edge)
        val trackWidth = 4.dp

        // Draw track and progress
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .width(trackWidth)
                .fillMaxHeight()
                .background(inactiveTrackColor, RoundedCornerShape(2.dp))
        ) {
            // Active part from top to current handle position
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(currentFraction)
                    .background(activeTrackColor, RoundedCornerShape(2.dp))
            )
        }

        // Draggable Teardrop Handle
        // The teardrop handle has width = 75.dp, height = 48.dp
        // Its vertical position is proportional to currentFraction
        val handleHeight = 48.dp
        val maxOffsetPx = totalHeightPx - with(LocalDensity.current) { handleHeight.toPx() }
        val handleOffsetPx = currentFraction * maxOffsetPx

        Box(
            modifier = Modifier
                .offset(
                    x = 0.dp, // It sits on the right, pointing left
                    y = with(LocalDensity.current) { handleOffsetPx.toDp() }
                )
                .align(Alignment.TopEnd)
                .pointerInput(totalPages) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            dragFraction = targetFraction
                        },
                        onDragEnd = {
                            isDragging = false
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newOffsetPx = (dragFraction * maxOffsetPx + dragAmount.y).coerceIn(0f, maxOffsetPx)
                            val newFraction = if (maxOffsetPx > 0f) newOffsetPx / maxOffsetPx else 0f
                            dragFraction = newFraction

                            val exactPage = 1 + (newFraction * (totalPages - 1))
                            val page = exactPage.roundToInt().coerceIn(1, totalPages)
                            if (page != currentPage) {
                                onPageChange(page)
                            }
                        }
                    )
                }
                .size(width = 75.dp, height = handleHeight)
                .drawBehind {
                    // Draw custom teardrop pointing left (bulb on the right, point on the left)
                    val r = size.height / 2f
                    val centerX = size.width - r
                    val path = Path().apply {
                        arcTo(
                            rect = Rect(centerX - r, 0f, centerX + r, size.height),
                            startAngleDegrees = 90f,
                            sweepAngleDegrees = -180f,
                            forceMoveTo = false
                        )
                        quadraticTo(
                            centerX - r * 0.3f, 0f,
                            0f, r
                        )
                        quadraticTo(
                            centerX - r * 0.3f, size.height,
                            centerX, size.height
                        )
                        close()
                    }
                    drawPath(
                        path = path,
                        color = accentPurple
                    )
                }
        ) {
            // Text centered inside the round bulb (the rightmost 48.dp)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${if (isDragging) (1 + (dragFraction * (totalPages - 1))).roundToInt().coerceIn(1, totalPages) else currentPage}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
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
    readingTheme: String,
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

                // Explicitly force hardware acceleration for incredibly smooth GPU-backed rendering and scrolling
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

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
                        applyPdfTheme(view, readingTheme)
                        injectPdfBridge(view)
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
        update = { webView ->
            applyPdfTheme(webView, readingTheme)
            injectPdfBridge(webView)
        },
        modifier = modifier
    )
}

private fun applyPdfTheme(webView: WebView?, theme: String) {
    val css = when (theme) {
        "dark" -> "body, #viewerContainer { background-color: #1E1F22 !important; } .page { background-color: #2B2D31 !important; filter: invert(0.9) hue-rotate(180deg) !important; }"
        "black" -> "body, #viewerContainer { background-color: #000000 !important; } .page { background-color: #111111 !important; filter: invert(1) hue-rotate(180deg) !important; }"
        "sepia" -> "body, #viewerContainer { background-color: #F4ECD8 !important; } .page { background-color: #FCF5E3 !important; filter: sepia(0.6) contrast(0.95) !important; }"
        else -> "body, #viewerContainer { background-color: #F4F4F4 !important; } .page { background-color: #FFFFFF !important; filter: none !important; }"
    }
    
    val fullCss = """
        html, body {
            overflow: hidden !important;
            height: 100% !important;
            width: 100% !important;
            margin: 0 !important;
            padding: 0 !important;
        }
        .toolbar, #toolbarContainer, #toolbarViewer, .findbar, #findbar, #findbarContainer, 
        #secondaryToolbar, .secondaryToolbar, .editorParamsToolbar, .doorHanger, .doorHangerRight {
            display: none !important;
            height: 0px !important;
            min-height: 0px !important;
            padding: 0 !important;
            margin: 0 !important;
            overflow: hidden !important;
            visibility: hidden !important;
        }
        #viewerContainer {
            position: absolute !important;
            inset: 0px !important;
            top: 0px !important;
            bottom: 0px !important;
            left: 0px !important;
            right: 0px !important;
            height: 100% !important;
            width: 100% !important;
            --visible-toolbar-height: 0px !important;
            overflow-y: auto !important;
            overflow-x: hidden !important;
            -webkit-overflow-scrolling: touch !important;
        }
        $css
    """.trimIndent().replace("\n", " ")

    webView?.evaluateJavascript(
        """
        (function() {
            let style = document.getElementById('pdf-custom-theme-style');
            if (!style) {
                style = document.createElement('style');
                style.id = 'pdf-custom-theme-style';
                document.head.appendChild(style);
            }
            style.textContent = `$fullCss`;
        })();
        """.trimIndent(),
        null
    )
}

private fun injectPdfBridge(webView: WebView?) {
    val js = """
        (function() {
            if (window.hasPdfBridgeInjected) return;
            window.hasPdfBridgeInjected = true;

            let lastReportedPage = -1;
            let lastReportedTotal = -1;
            let lastReportedMatchCurrent = -1;
            let lastReportedMatchTotal = -1;

            function parsePageNum(str) {
                if (!str) return 0;
                const arabicDigits = {
                    '٠': '0', '١': '1', '٢': '2', '٣': '3', '٤': '4',
                    '٥': '5', '٦': '6', '٧': '7', '٨': '8', '٩': '9'
                };
                let cleanStr = '';
                for (let i = 0; i < str.length; i++) {
                    const char = str[i];
                    if (arabicDigits[char] !== undefined) {
                        cleanStr += arabicDigits[char];
                    } else if (char >= '0' && char <= '9') {
                        cleanStr += char;
                    }
                }
                return parseInt(cleanStr) || 0;
            }

            function reportPage(p, t) {
                try {
                    if (window.AndroidBridge && typeof window.AndroidBridge.onPageChanged === "function") {
                        window.AndroidBridge.onPageChanged(p, t);
                        lastReportedPage = p;
                        lastReportedTotal = t;
                        return true;
                    }
                } catch (e) {
                    console.error("PDF_JS_REPORT_ERROR: " + e.message);
                }
                return false;
            }

            function reportSearchMatches(current, total) {
                try {
                    if (window.AndroidBridge && typeof window.AndroidBridge.onSearchMatchesCount === 'function') {
                        window.AndroidBridge.onSearchMatchesCount(current, total);
                        lastReportedMatchCurrent = current;
                        lastReportedMatchTotal = total;
                        return true;
                    }
                } catch (e) {
                    console.error("PDF_JS_REPORT_SEARCH_ERROR: " + e.message);
                }
                return false;
            }

            function poll() {
                if (!window.PDFViewerApplication) {
                    setTimeout(poll, 250);
                    return;
                }

                // 1. Poll Page and Total Pages
                try {
                    let page = 1;
                    if (window.PDFViewerApplication.pdfViewer && typeof window.PDFViewerApplication.pdfViewer.currentPageNumber === 'number') {
                        page = window.PDFViewerApplication.pdfViewer.currentPageNumber || 1;
                    } else if (typeof window.PDFViewerApplication.page === 'number') {
                        page = window.PDFViewerApplication.page || 1;
                    } else {
                        const pageNumEl = document.getElementById("pageNumber");
                        if (pageNumEl && pageNumEl.value) {
                            page = parsePageNum(pageNumEl.value) || 1;
                        }
                    }

                    let total = 0;
                    if (window.PDFViewerApplication.pdfDocument && typeof window.PDFViewerApplication.pdfDocument.numPages === 'number') {
                        total = window.PDFViewerApplication.pdfDocument.numPages;
                    } else if (window.PDFViewerApplication.pdfViewer && typeof window.PDFViewerApplication.pdfViewer.pagesCount === 'number') {
                        total = window.PDFViewerApplication.pdfViewer.pagesCount;
                    } else if (typeof window.PDFViewerApplication.pagesCount === 'number') {
                        total = window.PDFViewerApplication.pagesCount;
                    }
                    
                    if (!total) {
                        const numPagesEl = document.getElementById("numPages");
                        if (numPagesEl && numPagesEl.textContent) {
                            total = parsePageNum(numPagesEl.textContent) || 0;
                        }
                    }

                    const reportTotal = total || 1;

                    if (page !== lastReportedPage || reportTotal !== lastReportedTotal) {
                        reportPage(page, reportTotal);
                    }
                } catch (e) {
                    console.error("PDF_JS_POLL_PAGE_ERROR: " + e.message);
                }

                // 2. Poll Search Matches Count
                try {
                    if (window.PDFViewerApplication.findController) {
                        const fc = window.PDFViewerApplication.findController;
                        if (fc && fc._selected) {
                            const pageIdx = fc._selected.pageIdx;
                            const matchIdx = fc._selected.matchIdx;
                            let current = 0;
                            let matchTotal = fc._matchesCountTotal || 0;
                            if (typeof pageIdx === 'number' && typeof matchIdx === 'number' && matchIdx !== -1) {
                                for (let i = 0; i < pageIdx; i++) {
                                    current += (fc._pageMatches && fc._pageMatches[i]) ? fc._pageMatches[i].length : 0;
                                }
                                current += matchIdx + 1;
                            }
                            if (current < 1 || current > matchTotal) {
                                current = matchTotal = 0;
                            }

                            if (current !== lastReportedMatchCurrent || matchTotal !== lastReportedMatchTotal) {
                                reportSearchMatches(current, matchTotal);
                            }
                        }
                    }
                } catch (e) {
                    console.error("PDF_JS_POLL_FIND_ERROR: " + e.message);
                }

                setTimeout(poll, 250);
            }

            function registerEvents() {
                try {
                    if (window.PDFViewerApplication && window.PDFViewerApplication.eventBus) {
                        const registerBusEvents = (bus) => {
                            bus.on('pagechanging', (e) => {
                                let total = 0;
                                if (window.PDFViewerApplication.pdfDocument) {
                                    total = window.PDFViewerApplication.pdfDocument.numPages;
                                } else if (window.PDFViewerApplication.pagesCount) {
                                    total = window.PDFViewerApplication.pagesCount;
                                }
                                if (!total) {
                                    const numPagesEl = document.getElementById("numPages");
                                    if (numPagesEl && numPagesEl.textContent) {
                                        total = parsePageNum(numPagesEl.textContent);
                                    }
                                }
                                reportPage(e.pageNumber, total || 1);
                            });

                            bus.on('pagesinit', () => {
                                let page = (window.PDFViewerApplication.pdfViewer && window.PDFViewerApplication.pdfViewer.currentPageNumber) || 1;
                                let total = 0;
                                if (window.PDFViewerApplication.pdfDocument) {
                                    total = window.PDFViewerApplication.pdfDocument.numPages;
                                } else if (window.PDFViewerApplication.pagesCount) {
                                    total = window.PDFViewerApplication.pagesCount;
                                }
                                if (!total) {
                                    const numPagesEl = document.getElementById("numPages");
                                    if (numPagesEl && numPagesEl.textContent) {
                                        total = parsePageNum(numPagesEl.textContent);
                                    }
                                }
                                reportPage(page, total || 1);
                            });

                            bus.on('pagesloaded', (e) => {
                                let page = (window.PDFViewerApplication.pdfViewer && window.PDFViewerApplication.pdfViewer.currentPageNumber) || 1;
                                let total = e.pagesCount || (window.PDFViewerApplication.pdfDocument && window.PDFViewerApplication.pdfDocument.numPages) || 1;
                                reportPage(page, total);
                            });

                            bus.on('updatefindmatchescount', (e) => {
                                try {
                                    const current = (e.matchesCount && typeof e.matchesCount.current === 'number') ? e.matchesCount.current : 0;
                                    const total = (e.matchesCount && typeof e.matchesCount.total === 'number') ? e.matchesCount.total : 0;
                                    reportSearchMatches(current, total);
                                } catch (err) {}
                            });

                            bus.on('updatefindcontrolstate', (e) => {
                                try {
                                    if (window.AndroidBridge && typeof window.AndroidBridge.onSearchStateChanged === 'function') {
                                        window.AndroidBridge.onSearchStateChanged(e.state, e.previous);
                                    }
                                    if (e.matchesCount) {
                                        const current = typeof e.matchesCount.current === 'number' ? e.matchesCount.current : 0;
                                        const total = typeof e.matchesCount.total === 'number' ? e.matchesCount.total : 0;
                                        reportSearchMatches(current, total);
                                    }
                                } catch (err) {}
                            });
                        };

                        registerBusEvents(window.PDFViewerApplication.eventBus);
                    } else {
                        setTimeout(registerEvents, 100);
                    }
                } catch (e) {
                    console.error("PDF_JS_INIT_ERROR: " + e.message);
                }
            }

            poll();
            registerEvents();
        })();
    """.trimIndent()

    webView?.evaluateJavascript(js, null)
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

private fun getPdfPageCount(filePath: String): Int {
    var pfd: ParcelFileDescriptor? = null
    var renderer: PdfRenderer? = null
    return try {
        val file = java.io.File(filePath)
        if (!file.exists()) return 1
        pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        renderer = PdfRenderer(pfd)
        renderer.pageCount
    } catch (e: Exception) {
        android.util.Log.e("PdfPageCount", "Error reading PDF page count: ${e.message}", e)
        1
    } finally {
        try {
            renderer?.close()
        } catch (e: Exception) {}
        try {
            pfd?.close()
        } catch (e: Exception) {}
    }
}
