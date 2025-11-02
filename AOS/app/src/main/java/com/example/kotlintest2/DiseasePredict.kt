package com.example.kotlintest2

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


// 1. 추론 결과를 담을 데이터 클래스 정의
data class PredictionResult(
    val className: String,
    val confidence: Float // 0.0 ~ 1.0 사이의 값
)

// 2. 추론기 클래스 (Android Activity가 아님!)
// (참고: 이름이 Activity라 혼동될 수 있으니, 나중에 ImagePredictor 등으로 바꾸는 것을 권장합니다.)
class DiseasePredictor(private val context: Context) {

    companion object {
        private const val TAG = "DiseasePredict"
        // DINOv2/ImageNet 표준 전처리 값 (학습 때 사용한 값과 동일해야 함)
        private val NORM_MEAN_RGB = floatArrayOf(0.485f, 0.456f, 0.406f)
        private val NORM_STD_RGB = floatArrayOf(0.229f, 0.224f, 0.225f)
        private val CLASS_NAMES = listOf("downy", "healthy", "powdery") // 클래스 순서 중요
    }

    private var module: Module? = null

    // 3. 모델 로드 함수 (Fragment에서 호출)
    fun initModel() {
        try {
            val modelPath = assetFilePath(context, "dinov2_mobile.ptl")
            Log.i(TAG, "모델 로드 경로: $modelPath")
            module = LiteModuleLoader.load(modelPath)
            Log.i(TAG, "모델 로드 성공: $modelPath")
        } catch (e: Exception) {
            Log.e(TAG, "모델 로드 실패", e)
        }
    }

    // 4. 비트맵으로 추론하는 함수 (핵심)
    fun predict(bitmap: Bitmap): PredictionResult? {
        if (module == null) {
            Log.e(TAG, "모델이 로드되지 않았습니다. initModel()을 먼저 호출하세요.")
            return null
        }

        val startTime = SystemClock.elapsedRealtime()

        // --- 4-1. 입력 이미지 전처리 (PyTorch Mobile과 동일하게) ---
        // 1. 256x256 리사이즈 (혹은 짧은 쪽 256)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true)
        // 2. 224x224 중앙 크롭
        val startX = (resizedBitmap.width - 224) / 2
        val startY = (resizedBitmap.height - 224) / 2
        val croppedBitmap = Bitmap.createBitmap(resizedBitmap, startX, startY, 224, 224)

        // 3. Bitmap -> Tensor (및 정규화)
        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            croppedBitmap,
            NORM_MEAN_RGB,
            NORM_STD_RGB
        )

        // --- 4-2. 모델 추론 실행 ---
        val output: IValue = module!!.forward(IValue.from(inputTensor))
        val scores: FloatArray = output.toTensor().dataAsFloatArray // [1.2, 5.5, 0.3] 같은 raw 점수

        // --- 4-3. 결과 후처리 (Softmax + Argmax) ---
        // Softmax를 적용하여 확률로 변환 (선택 사항이지만 신뢰도 표시에 유용)
        val probabilities = softmax(scores)

        var maxScore = -Float.MAX_VALUE
        var maxScoreIdx = -1
        for (i in probabilities.indices) {
            if (probabilities[i] > maxScore) {
                maxScore = probabilities[i]
                maxScoreIdx = i
            }
        }

        val inferenceTime = SystemClock.elapsedRealtime() - startTime
        Log.d(TAG, "추론 시간: ${inferenceTime}ms")

        return PredictionResult(
            className = CLASS_NAMES[maxScoreIdx],
            confidence = maxScore
        )
    }

    // Softmax 함수 (raw score를 0.0 ~ 1.0 확률로 변환)
    private fun softmax(scores: FloatArray): FloatArray {
        val expScores = scores.map { kotlin.math.exp(it) }.toFloatArray()
        val sumExpScores = expScores.sum()
        return expScores.map { it / sumExpScores }.toFloatArray()
    }

    // assets 폴더의 파일 경로를 가져오는 헬퍼 함수
    @Throws(IOException::class)
    private fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }

        context.assets.open(assetName).use { `is` ->
            FileOutputStream(file).use { os ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (`is`.read(buffer).also { read = it } != -1) {
                    os.write(buffer, 0, read)
                }
                os.flush()
            }
            return file.absolutePath
        }
    }
}