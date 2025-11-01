package com.example.kotlintest2

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream

class ResultActivity : AppCompatActivity() {

    private lateinit var resultImageView: ImageView
    private lateinit var diseaseNameTextView: TextView
    private lateinit var confidenceTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // 뷰 초기화
        resultImageView = findViewById(R.id.resultImageView)
        diseaseNameTextView = findViewById(R.id.diseaseNameTextView)
        confidenceTextView = findViewById(R.id.confidenceTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        backButton = findViewById(R.id.backButton)

        // Intent로 전달받은 데이터 처리
        val imageUriString = intent.getStringExtra("imageUri")
        val diseaseName = intent.getStringExtra("diseaseName") ?: "노균병"
        val confidence = intent.getIntExtra("confidence", 92)
        val imageResId = intent.getIntExtra("imageResId", -1)
        val fromHistory = intent.getBooleanExtra("fromHistory", false)

        if (fromHistory && imageResId != -1) {
            // 히스토리에서 온 경우 리소스 ID로 이미지 표시
            resultImageView.setImageResource(imageResId)
        } else {
            // 카메라에서 온 경우 기존 로직
            imageUriString?.let {
                val imageUri = Uri.parse(it)
                loadAndRotateImage(imageUri)
            }
        }

        // 이미지 표시 (회전 처리 포함)
        imageUriString?.let {
            val imageUri = Uri.parse(it)
            loadAndRotateImage(imageUri)
        }

        // 결과 데이터 표시
        displayResult(diseaseName, confidence)

        // 버튼 이벤트
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadAndRotateImage(imageUri: Uri) {
        try {
            // 이미지 로드
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            var bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // EXIF 정보로 회전 각도 확인
            val exifInputStream: InputStream? = contentResolver.openInputStream(imageUri)
            exifInputStream?.let { stream ->
                val exif = ExifInterface(stream)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )

                // 회전 각도 계산
                val rotation = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }

                // 이미지 회전
                if (rotation != 0f) {
                    bitmap = rotateBitmap(bitmap, rotation)
                }

                stream.close()
            }

            // 이미지 설정
            resultImageView.setImageBitmap(bitmap)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun rotateBitmap(bitmap: Bitmap?, degrees: Float): Bitmap? {
        if (bitmap == null) return null

        val matrix = Matrix()
        matrix.postRotate(degrees)

        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    private fun displayResult(diseaseName: String, confidence: Int) {
        // 병변명 설정
        diseaseNameTextView.text = diseaseName

        // 신뢰도 설정
        confidenceTextView.text = "${confidence}%"

        // 병변에 따른 색상 및 설명 설정
        when {
            diseaseName.contains("노균병") -> {
                diseaseNameTextView.setTextColor(getColor(R.color.disease_yellow))
                confidenceTextView.setTextColor(getColor(R.color.disease_yellow))
                descriptionTextView.text = "잎 표면에 처음에는 퇴록된 부정형 반점이\n생기고, 감염부위가 담황색을 띕니다."
            }
            diseaseName.contains("정상") -> {
                diseaseNameTextView.setTextColor(getColor(R.color.primary_green))
                confidenceTextView.setTextColor(getColor(R.color.primary_green))
                descriptionTextView.text = "정상적인 잎입니다."
            }
            else -> {
                diseaseNameTextView.setTextColor(getColor(R.color.primary_green))
                confidenceTextView.setTextColor(getColor(R.color.primary_green))
                descriptionTextView.text = "AI 모델 분석 결과입니다."
            }
        }
    }
}