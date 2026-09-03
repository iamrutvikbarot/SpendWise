package com.example.ui.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.ui.components.GradientButton
import com.example.ui.components.SecondaryGlassButton
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.Executors

enum class ScanMode {
    CAMERA_LIVE,
    GALLERY_PREVIEW
}

@Composable
fun ScannerScreen(
    viewModel: com.example.ui.transactions.TransactionViewModel,
    initialUri: Uri? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val ocrProcessor = remember { OCRProcessor() }
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    var scanMode by remember { mutableStateOf(if (initialUri != null) ScanMode.GALLERY_PREVIEW else ScanMode.CAMERA_LIVE) }
    var imageUri by remember { mutableStateOf<Uri?>(initialUri) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var extractedData by remember { mutableStateOf<ExtractedTransaction?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var scanError by remember { mutableStateOf<String?>(null) }

    var isFlashOn by remember { mutableStateOf(false) }
    var boundCamera by remember { mutableStateOf<Camera?>(null) }

    // Editable fields for user verification before saving
    var editableAmount by remember { mutableStateOf("") }
    var editableMerchant by remember { mutableStateOf("") }
    var editableCategory by remember { mutableStateOf("Bills") }
    var editablePaymentMethod by remember { mutableStateOf("UPI") }

    // Camera permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            scanMode = ScanMode.GALLERY_PREVIEW
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission && initialUri == null) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val updateExtractedState: (ExtractedTransaction?) -> Unit = { result ->
        isProcessing = false
        if (result != null) {
            extractedData = result
            val amt = result.amount ?: 0.0
            editableAmount = if (amt > 0) {
                if (amt % 1.0 == 0.0) amt.toInt().toString() else amt.toString()
            } else ""
            editableMerchant = (result.merchantName ?: "Indane Gas").replace("\r", " ").replace("\n", " ").trim().replace(Regex("\\s+"), " ")
            editableCategory = result.category.replace("\r", " ").replace("\n", " ").trim().ifBlank { "Bills" }
            editablePaymentMethod = result.paymentMethod.replace("\r", " ").replace("\n", " ").trim().ifBlank { "UPI" }
            if (amt <= 0) {
                scanError = "Please verify and enter the final amount."
            } else {
                scanError = null
            }
        } else {
            scanError = "Could not extract text. Please try again with clear lighting."
        }
    }

    // Process picked/shared bitmap
    val processUri: (Uri) -> Unit = { uri ->
        isProcessing = true
        scanError = null
        capturedBitmap = null
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            val softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            ocrProcessor.processImage(softwareBitmap) { result ->
                (context as? android.app.Activity)?.runOnUiThread {
                    updateExtractedState(result)
                }
            }
        } catch (e: Exception) {
            Log.e("ScannerScreen", "Error reading bitmap", e)
            isProcessing = false
            scanError = "Failed to process image file."
        }
    }

    LaunchedEffect(initialUri) {
        initialUri?.let {
            imageUri = it
            scanMode = ScanMode.GALLERY_PREVIEW
            processUri(it)
        }
    }

    // Gallery Picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            capturedBitmap = null
            scanMode = ScanMode.GALLERY_PREVIEW
            processUri(uri)
        }
    }

    // Camera capture instance
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    val captureLiveFrame: () -> Unit = {
        val capture = imageCapture
        if (capture != null) {
            isProcessing = true
            scanError = null
            capture.takePicture(
                cameraExecutor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                        val bitmap = ocrProcessor.imageProxyToBitmap(imageProxy)
                        if (bitmap != null) {
                            (context as? android.app.Activity)?.runOnUiThread {
                                capturedBitmap = bitmap
                            }
                            ocrProcessor.processImage(bitmap) { result ->
                                (context as? android.app.Activity)?.runOnUiThread {
                                    updateExtractedState(result)
                                }
                            }
                        } else {
                            imageProxy.close()
                            (context as? android.app.Activity)?.runOnUiThread {
                                isProcessing = false
                                scanError = "Could not capture image from camera."
                            }
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("ScannerScreen", "Camera capture failed", exception)
                        (context as? android.app.Activity)?.runOnUiThread {
                            isProcessing = false
                            scanError = "Camera capture failed: ${exception.message}"
                        }
                    }
                }
            )
        }
    }

    val categories = listOf("Bills", "Food", "Shopping", "Transport", "Health", "Entertainment", "Other")
    val paymentMethods = listOf("Amazon Pay", "UPI", "Credit Card", "Debit Card", "Net Banking", "Wallet", "Cash")

    val isResultReady = extractedData != null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // =========================================================================
        // FULL SCREEN LIVE CAMERA VIEW (When in live mode and result is not extracted yet)
        // =========================================================================
        if (scanMode == ScanMode.CAMERA_LIVE && !isResultReady && capturedBitmap == null) {
            if (hasCameraPermission) {
                // Full Screen Camera Surface
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            imageCapture = capture

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                val cam = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    capture
                                )
                                boundCamera = cam
                            } catch (e: Exception) {
                                Log.e("ScannerScreen", "Camera binding failed", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Laser Scanner Animation
                val infiniteTransition = rememberInfiniteTransition(label = "fullscreen_laser")
                val laserProgress by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2400, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "laser_progress"
                )

                // Viewfinder Target Overlay on Camera
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                ) {
                    val boxWidth = maxWidth * 0.86f
                    val boxHeight = maxHeight * 0.58f

                    // Semi-transparent darkened background with cutout frame
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Reticle Box
                        Box(
                            modifier = Modifier
                                .size(width = boxWidth, height = boxHeight)
                                .clip(RoundedCornerShape(20.dp))
                                .border(2.dp, PrimaryTeal.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                        ) {
                            // Animated Laser Line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .offset(y = (laserProgress * (boxHeight.value - 10)).dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.Transparent,
                                                PrimaryTeal,
                                                PrimaryTeal,
                                                Color(0xFF6EE7B7),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                    }

                    // Floating Instruction Pill
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 70.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .border(1.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = PrimaryTeal,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Align full bill or receipt inside frame",
                                color = MaterialTheme.colorScheme.surface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Top Bar Controls (Back, Title, Flash)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .border(1.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.25f), CircleShape)
                                .clickable(onClick = onNavigateBack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Mode Indicator Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .border(1.dp, PrimaryTeal.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Scan Bill",
                                color = MaterialTheme.colorScheme.surface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Torch / Flash Toggle
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isFlashOn) PrimaryTeal else Color.Black.copy(alpha = 0.6f))
                                .border(1.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.25f), CircleShape)
                                .clickable {
                                    val next = !isFlashOn
                                    isFlashOn = next
                                    boundCamera?.cameraControl?.enableTorch(next)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFlashOn) Icons.Outlined.FlashOn else Icons.Outlined.FlashOff,
                                contentDescription = "Toggle Flash",
                                tint = if (isFlashOn) Color.Black else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Bottom Shutter & Gallery Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 24.dp)
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gallery Shortcut Button
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                                .border(1.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.3f), CircleShape)
                                .clickable {
                                    scanMode = ScanMode.GALLERY_PREVIEW
                                    capturedBitmap = null
                                    extractedData = null
                                    galleryLauncher.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Gallery",
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Large Shutter Capture Button
                        val shutterInteraction = remember { MutableInteractionSource() }
                        val isShutterPressed by shutterInteraction.collectIsPressedAsState()
                        val shutterScale by animateFloatAsState(
                            targetValue = if (isShutterPressed) 0.90f else 1f,
                            animationSpec = spring(),
                            label = "fullscreen_shutter_scale"
                        )

                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .scale(shutterScale)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                                .border(3.5.dp, PrimaryTeal, CircleShape)
                                .padding(6.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(PrimaryTeal, PrimaryTealDark)))
                                .clickable(
                                    interactionSource = shutterInteraction,
                                    indication = null,
                                    enabled = !isProcessing,
                                    onClick = captureLiveFrame
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Capture Bill",
                                tint = Color(0xFF070B14),
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        // Empty spacer to balance layout
                        Spacer(modifier = Modifier.size(50.dp))
                    }
                }
            } else {
                // Permission Request Screen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = PrimaryTeal,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Text(
                            text = "Camera Access Needed",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Grant camera permission to scan bills and receipts directly in full screen.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        GradientButton(
                            text = "Enable Camera",
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                        )

                        TextButton(
                            onClick = {
                                scanMode = ScanMode.GALLERY_PREVIEW
                                galleryLauncher.launch("image/*")
                            }
                        ) {
                            Text("Pick from Gallery Instead", color = PrimaryTeal)
                        }
                    }
                }
            }
        } else {
            // =========================================================================
            // RESULT & EDIT DETAILS VIEW OR GALLERY PICKER VIEW
            // =========================================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 36.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable(onClick = onNavigateBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AI Bill Scanner",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = PrimaryTeal,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = if (isResultReady) "Verification & Save" else "Select Bill",
                            color = PrimaryTeal,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }

                    // Retake / Switch to Camera Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable {
                                scanMode = ScanMode.CAMERA_LIVE
                                capturedBitmap = null
                                extractedData = null
                                imageUri = null
                                if (!hasCameraPermission) {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = PrimaryTeal,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Camera",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Captured Receipt Snapshot / Preview
                    if (capturedBitmap != null || imageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, PrimaryTeal.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (capturedBitmap != null) {
                                Image(
                                    bitmap = capturedBitmap!!.asImageBitmap(),
                                    contentDescription = "Receipt Snapshot",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (imageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(imageUri),
                                    contentDescription = "Receipt Screenshot",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Overlay Gradient with badge
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                        )
                                    )
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Bill Captured",
                                    color = MaterialTheme.colorScheme.surface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Scanned Details Verification Card
                    val data = extractedData
                    if (data != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0xFF000000))
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, PrimaryTeal.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = PrimaryTeal,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Scanned Details",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PrimaryTeal.copy(alpha = 0.15f))
                                            .border(1.dp, PrimaryTeal.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = data.confidenceNotes.ifEmpty { data.platform ?: "AI Extracted" },
                                            color = PrimaryTeal,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Divider(color = MaterialTheme.colorScheme.outline)

                                // Editable Amount Field
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Total Amount Paid", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    OutlinedTextField(
                                        value = editableAmount,
                                        onValueChange = { editableAmount = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = MaterialTheme.typography.titleLarge.copy(
                                            color = ExpenseRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        ),
                                        leadingIcon = {
                                            Text(
                                                text = "₹",
                                                color = ExpenseRed,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(start = 12.dp)
                                            )
                                        },
                                        placeholder = { Text("0.00", color = TextTertiary) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PrimaryTeal,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                            cursorColor = PrimaryTeal
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                // Editable Merchant / Provider
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Merchant / Provider", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    OutlinedTextField(
                                        value = editableMerchant,
                                        onValueChange = { editableMerchant = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        ),
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Storefront,
                                                contentDescription = null,
                                                tint = PrimaryTeal,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        placeholder = { Text("e.g. Indane Gas, Amazon Pay", color = TextTertiary) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PrimaryTeal,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                            cursorColor = PrimaryTeal
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                // Category Selector Chips
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Category", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(categories) { cat ->
                                            val isSelected = editableCategory.equals(cat, ignoreCase = true)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (isSelected) PrimaryTeal.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) PrimaryTeal else MaterialTheme.colorScheme.outline,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable { editableCategory = cat }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = cat,
                                                    color = if (isSelected) PrimaryTeal else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }

                                // Payment Method Selector Chips
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Payment Method", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(paymentMethods) { method ->
                                            val isSelected = editablePaymentMethod.contains(method, ignoreCase = true)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (isSelected) PrimaryTeal.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) PrimaryTeal else MaterialTheme.colorScheme.outline,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable { editablePaymentMethod = method }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = method,
                                                    color = if (isSelected) PrimaryTeal else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }

                                if (!data.transactionId.isNullOrEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Ref ID", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                        Text(
                                            text = data.transactionId,
                                            color = TextTertiary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        val finalParsedAmount = editableAmount.toDoubleOrNull() ?: 0.0

                        // Save CTA
                        GradientButton(
                            text = "Save Expense (${if (finalParsedAmount > 0) format.format(finalParsedAmount) else "₹0.00"})",
                            onClick = {
                                val amount = finalParsedAmount
                                val title = editableMerchant.ifBlank { "Indane Gas Bill" }
                                val category = editableCategory
                                val platform = editablePaymentMethod
                                viewModel.addTransaction(amount, title, true, category, platform, onNavigateBack)
                            },
                            enabled = finalParsedAmount > 0,
                            gradientColors = listOf(ExpenseRed, Color(0xFFE11D48)),
                            height = 48.dp
                        )

                        SecondaryGlassButton(
                            text = "Scan Another Bill / Receipt",
                            onClick = {
                                extractedData = null
                                capturedBitmap = null
                                editableAmount = ""
                                editableMerchant = ""
                                scanError = null
                                scanMode = ScanMode.CAMERA_LIVE
                                if (!hasCameraPermission) {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            height = 44.dp
                        )
                    } else if (scanMode == ScanMode.GALLERY_PREVIEW) {
                        // Gallery Empty State Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        tint = PrimaryTeal,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Text(
                                    text = "Choose Receipt Screenshot",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Select any payment screenshot, PDF receipt image, or bill photo from your device.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.5.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                GradientButton(
                                    text = "Browse Images",
                                    onClick = { galleryLauncher.launch("image/*") },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.PhotoLibrary,
                                            contentDescription = null,
                                            tint = Color(0xFF090D16),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    if (scanError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = scanError ?: "",
                                color = Color(0xFFFCA5A5),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // FULL-SCREEN PROCESSING OVERLAY
        // =========================================================================
        if (isProcessing) {
            val infiniteTransition = rememberInfiniteTransition(label = "fullscreen_processing")
            val ringRotate by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "ring_rotate"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.size(90.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier
                                .size(80.dp)
                                .rotate(ringRotate)
                        ) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(
                                        PrimaryTeal,
                                        PrimaryTeal,
                                        Color.Transparent
                                    )
                                ),
                                startAngle = 0f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = 3.5.dp.toPx())
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Analyzing Receipt...",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Extracting merchant, total amount & category",
                            color = PrimaryTeal,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
