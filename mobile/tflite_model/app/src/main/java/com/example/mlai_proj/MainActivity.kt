package com.example.mlai_proj

import android.Manifest
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    private val cameraExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

        setContent {
            val context = LocalContext.current
            var hasPermission by remember { mutableStateOf(false) }
            var overlayText by remember { mutableStateOf("Requesting camera permission…") }

            LaunchedEffect(Unit) {
                requestPermission.launch(Manifest.permission.CAMERA)
            }

            hasPermission =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            Column(modifier = Modifier.fillMaxSize()) {

                // Overlay bar at the top
                Text(
                    text = overlayText,
                    color = Color(0xFF00FF00),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xAA000000))
                        .padding(12.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                if (hasPermission) {
                    CameraPreview(
                        onPredictionText = { overlayText = it },
                        cameraExecutor = cameraExecutor
                    )
                } else {
                    Text(
                        text = "Camera permission denied",
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@Composable
private fun CameraPreview(
    onPredictionText: (String) -> Unit,
    cameraExecutor: java.util.concurrent.ExecutorService
) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    val classifier = remember { TFLiteClassifier(context) }

    val config = LocalConfiguration.current
    val side = config.screenWidthDp.dp

    Box(
        modifier = Modifier
            .size(side) // debug border, remove later
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    var frameCount = 0
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        try {
                            frameCount++
                            if (frameCount % 2 != 0) return@setAnalyzer

                            val bmp = imageProxy.toBitmap()
                            val square = classifier.centreCropSquare(bmp)
                            val resized = classifier.resizeTo160(square)

                            val result = classifier.predict(resized)
                            val p = result.probs

                            val line1 =
                                "Pred: ${result.label} (${String.format("%.1f", result.confidence * 100f)}%)"
                            val line2 =
                                "onion_brown: %.2f | onion_purple: %.2f | peach: %.2f | unknown: %.2f"
                                    .format(p[0], p[1], p[2], p[3])

                            onPredictionText("$line1\n$line2")
                        } finally {
                            imageProxy.close()
                        }
                    }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                    )
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )
    }
}
