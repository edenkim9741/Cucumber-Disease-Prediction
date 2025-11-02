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
import android.widget.Button
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

class CameraFragment : Fragment() {

    private lateinit var previewView: PreviewView
    private lateinit var captureButton: Button
    private lateinit var cameraExecutor: ExecutorService
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

        cameraExecutor = Executors.newSingleThreadExecutor()

        predictor = DiseasePredictor(requireContext())

        cameraExecutor.execute {
            predictor.initModel()
        }


        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        captureButton.setOnClickListener { takePhoto() }
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

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            requireActivity().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "사진 촬영 실패: ${exception.message}", exception)
                    Toast.makeText(context, "사진 촬영 실패", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(context, "사진 촬영 완료!", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "사진이 저장되었습니다: ${output.savedUri}")

                    output.savedUri?.let { uri ->

                        // 추론은 무거운 작업이므로 백그라운드 스레드(cameraExecutor)에서 실행
                        cameraExecutor.execute {
                            try {
                                // 1. 저장된 URI를 Bitmap으로 변환
                                val bitmap = MediaStore.Images.Media.getBitmap(
                                    requireActivity().contentResolver,
                                    uri
                                )

                                // 2. Predictor로 추론 실행
                                val prediction = predictor.predict(bitmap)

                                // 3. 결과 로깅 (요청 사항)
                                if (prediction != null) {
                                    Log.i(TAG, "--- 추론 결과 ---")
                                    Log.i(TAG, "URI: $uri")
                                    Log.i(TAG, "클래스: ${prediction.className}")
                                    Log.i(TAG, "신뢰도: ${prediction.confidence * 100}%")
                                    Log.i(TAG, "--------------------")

                                    // 4. 결과 페이지로 이동 (UI 작업이므로 Main 스레드에서 실행)
                                    activity?.runOnUiThread {
                                        navigateToResultPage(uri, prediction)
                                    }
                                } else {
                                    Log.e(TAG, "추론 결과가 null입니다.")
                                    activity?.runOnUiThread {
                                        Toast.makeText(context, "분석 실패", Toast.LENGTH_SHORT).show()
                                    }
                                }

                            } catch (e: Exception) {
                                Log.e(TAG, "추론 또는 비트맵 변환 실패", e)
                                activity?.runOnUiThread {
                                    Toast.makeText(context, "분석 중 오류 발생", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

//                    // 결과 페이지로 이동
//                    output.savedUri?.let { uri ->
//                        navigateToResultPage(uri)
//                    }
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
//        cameraExecutor.shutdown()
    }
}