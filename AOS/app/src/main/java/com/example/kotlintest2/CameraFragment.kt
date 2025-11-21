package com.example.kotlintest2

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.jvm.java
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import java.io.File
import kotlin.text.insert

class CameraFragment : Fragment() {

    private lateinit var historyManager: HistoryManager
    private lateinit var previewView: PreviewView
    private lateinit var captureButton: ImageButton
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var cameraFrame: View
    private var imageCapture: ImageCapture? = null

    private lateinit var predictor: DiseasePredictor

    companion object {
        private const val TAG = "CameraFragment"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(context, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }

    private fun navigateToResultPage(imageUri: Uri, prediction: PredictionResult) {
        val intent = Intent(requireActivity(), ResultActivity::class.java)
        intent.putExtra("imageUri", imageUri.toString())

        intent.putExtra("diseaseName", prediction.className)
        intent.putExtra("confidence", (prediction.confidence*100).toInt())

        startActivity(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        previewView = view.findViewById(R.id.previewView)
        captureButton = view.findViewById(R.id.captureButton)

        cameraFrame = view.findViewById(R.id.cameraFrame)

        // HistoryManager 초기화 추가
        historyManager = HistoryManager(requireContext())

        captureButton.setOnClickListener {
            takePhoto()
        }

        val logoText = view.findViewById<TextView>(R.id.appLogo)

        // Quantico 폰트 적용
        logoText.typeface = ResourcesCompat.getFont(requireContext(), R.font.quantico_bold)

        // 투톤 색상 적용
        val logoString = "QcumbeR"
        val spannableString = SpannableString(logoString)

        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.dark_green)),
            0, 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.dark_green)),
            6, 7,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        logoText.text = spannableString

        cameraExecutor = Executors.newSingleThreadExecutor()
        predictor = DiseasePredictor(requireContext())
        cameraExecutor.execute { predictor.initModel() }

        cameraExecutor.execute {
            predictor.initModel()
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()) {
            startCamera()   // 🔥 복귀 시 카메라 재시작
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            val cameraProvider = ProcessCameraProvider.getInstance(requireContext()).get()
            cameraProvider.unbindAll()  // 🔥 떠날 때 카메라 해제
        } catch (_: Exception) {}
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder().build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )

                Log.d(TAG, "카메라 시작 성공")

            } catch (exc: Exception) {
                Log.e(TAG, "카메라 시작 실패", exc)
                Toast.makeText(context, "카메라 시작 실패", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    // 외부에서 호출할 수 있는 공개 메서드
    fun takePhoto() {
        val imageCapture = imageCapture ?: run {
            Toast.makeText(context, "카메라가 준비되지 않았습니다", Toast.LENGTH_SHORT).show()
            return
        }

        val viewWidth = previewView.width
        val viewHeight = previewView.height

        val frameRect = android.graphics.Rect(
            cameraFrame.left,
            cameraFrame.top,
            cameraFrame.right,
            cameraFrame.bottom
        )

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "사진 촬영 실패: ${exception.message}", exception)
                }

                override fun onCaptureSuccess(image: ImageProxy) {
                    cameraExecutor.execute {
                        try {
                            processAndSaveImage(image, viewWidth, viewHeight, frameRect)
                        } catch (e: Exception) {
                            Log.e(TAG, "이미지 처리 실패" , e)
                        } finally {
                            image.close()
                        }
                    }
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    // processAndSaveImage 메서드 수정
    private fun processAndSaveImage(
        imageProxy: ImageProxy,
        viewWidth: Int,
        viewHeight: Int,
        frameRect: android.graphics.Rect
    ) {
        // 1. ImageProxy -> Bitmap 변환
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val originalBitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        // 2. 회전 보정
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val rotatedBitmap = if (rotationDegrees != 0) {
            val matrix = android.graphics.Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            android.graphics.Bitmap.createBitmap(
                originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true
            )
        } else {
            originalBitmap
        }

        // 3. 크롭 좌표 계산
        val widthRatio = rotatedBitmap.width.toFloat() / viewWidth
        val heightRatio = rotatedBitmap.height.toFloat() / viewHeight
        val scale = kotlin.math.min(widthRatio, heightRatio)

        val scaledViewWidth = viewWidth * scale
        val scaledViewHeight = viewHeight * scale

        val dx = (rotatedBitmap.width - scaledViewWidth) / 2
        val dy = (rotatedBitmap.height - scaledViewHeight) / 2

        var cropX = (frameRect.left * scale + dx).toInt()
        var cropY = (frameRect.top * scale + dy).toInt()
        var cropW = (frameRect.width() * scale).toInt()
        var cropH = (frameRect.height() * scale).toInt()

        // 범위 예외 처리
        if (cropX < 0) cropX = 0
        if (cropY < 0) cropY = 0
        if (cropX + cropW > rotatedBitmap.width) cropW = rotatedBitmap.width - cropX
        if (cropY + cropH > rotatedBitmap.height) cropH = rotatedBitmap.height - cropY

        // 4. 비트맵 자르기
        val croppedBitmap = android.graphics.Bitmap.createBitmap(rotatedBitmap, cropX, cropY, cropW, cropH)

        // 5. 갤러리에 저장
        val savedUri = saveBitmapToGallery(croppedBitmap)

        // 6. UI 업데이트 및 추론 시작
        if (savedUri != null) {
            // Predictor 추론
            val prediction = predictor.predict(croppedBitmap)

            activity?.runOnUiThread {
                Toast.makeText(context, "사진 저장 완료!", Toast.LENGTH_SHORT).show()
                if (prediction != null) {
                    // 히스토리에 저장 (추가된 부분)
                    historyManager.addHistoryItem(
                        imageUri = savedUri.toString(),
                        diseaseName = prediction.className,
                        confidence = (prediction.confidence * 100).toInt()
                    )

                    navigateToResultPage(savedUri, prediction)
                } else {
                    Toast.makeText(context, "분석 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveBitmapToGallery(bitmap: android.graphics.Bitmap): Uri? {
        val filename = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        val resolver = requireContext().contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        return uri?.also {
            resolver.openOutputStream(it).use { outputStream ->
                if (outputStream != null) {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            }
        }
    }
}



