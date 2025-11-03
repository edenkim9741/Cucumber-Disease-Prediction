package com.example.kotlintest2

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import org.pytorch.executorch.EValue;
import org.pytorch.executorch.Module;
import org.pytorch.executorch.Tensor;
import kotlin.math.exp

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
        private val CLASS_NAMES = listOf("노균병", "정상", "흰가루병") // 클래스 순서 중요
    }

    private lateinit var module: Module

    // 3. 모델 로드 함수 (Fragment에서 호출)
    fun initModel() {
        try {
            val modelPath = assetFilePath(context, "model.pte")
            Log.i(TAG, "모델 로드 경로: $modelPath")
            module = Module.load(modelPath)
            Log.i(TAG, "모델 로드 성공: $modelPath")
        } catch (e: Exception) {
            Log.e(TAG, "모델 로드 실패", e)
        }
    }

    fun predict(bitmap: Bitmap): PredictionResult {
        // Bitmap → FloatArray 변환
        val inputTensor = preprocessImage(bitmap)

        val inputValue = EValue.from(inputTensor)
        val output = module.forward(inputValue)
        val outputTensor = output[0].toTensor()
        val scores = outputTensor.getDataAsFloatArray()

        //print score in log one by one
        Log.i(TAG, "모델 추론 결과:")
        for (i in scores.indices) {
            Log.i(TAG, "${CLASS_NAMES[i]}: ${scores[i]}")
        }


        // 3️⃣ Softmax 후 가장 높은 확률의 클래스 선택
        val probs = softmax(scores)
        val maxIdx = probs.indices.maxByOrNull { probs[it] } ?: 0
        val label = CLASS_NAMES[maxIdx]

        return PredictionResult(label, probs[maxIdx])
    }

    private fun preprocessImage(bitmap: Bitmap): Tensor {
        // 224x224 크기로 리사이즈
        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        val inputTensor = FloatArray(1 * 3 * 224 * 224)
        val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
        val std = floatArrayOf(0.229f, 0.224f, 0.225f)

        var idx = 0
        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val pixel = resized.getPixel(x, y)
                val r = ((pixel shr 16 and 0xFF) / 255.0f - mean[0]) / std[0]
                val g = ((pixel shr 8 and 0xFF) / 255.0f - mean[1]) / std[1]
                val b = ((pixel and 0xFF) / 255.0f - mean[2]) / std[2]
                inputTensor[idx++] = r
                inputTensor[idx++] = g
                inputTensor[idx++] = b
            }
        }

        val shape = longArrayOf(1, 3, 224, 224)
        return Tensor.fromBlob(inputTensor, shape)
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val expValues = logits.map { exp(it.toDouble()).toFloat() }
        val sum = expValues.sum()
        return expValues.map { it / sum }.toFloatArray()
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