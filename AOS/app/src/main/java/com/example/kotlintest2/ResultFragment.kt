package com.example.kotlintest2

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import java.io.InputStream

class ResultFragment : Fragment() {

    companion object {
        private const val TAG = "ResultFragment"
        private const val ARG_IMAGE_URI = "imageUri"
        private const val ARG_IMAGE_RES_ID = "imageResId"
        private const val ARG_DISEASE_NAME = "diseaseName"
        private const val ARG_CONFIDENCE = "confidence"
        private const val ARG_FROM_HISTORY = "fromHistory"

        fun newInstance(
            imageUri: String?,
            imageResId: Int,
            diseaseName: String,
            confidence: Int,
            fromHistory: Boolean
        ): ResultFragment {
            val fragment = ResultFragment()
            val args = Bundle()
            args.putString(ARG_IMAGE_URI, imageUri)
            args.putInt(ARG_IMAGE_RES_ID, imageResId)
            args.putString(ARG_DISEASE_NAME, diseaseName)
            args.putInt(ARG_CONFIDENCE, confidence)
            args.putBoolean(ARG_FROM_HISTORY, fromHistory)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUriString = arguments?.getString(ARG_IMAGE_URI)
        val imageResId = arguments?.getInt(ARG_IMAGE_RES_ID) ?: -1
        val diseaseName = arguments?.getString(ARG_DISEASE_NAME)!!
        val confidence = arguments?.getInt(ARG_CONFIDENCE)!!
        val fromHistory = arguments?.getBoolean(ARG_FROM_HISTORY) ?: false

        // 뷰 초기화
        val logoTop = view.findViewById<TextView>(R.id.logoTop)
        val resultImageView = view.findViewById<ImageView>(R.id.resultImageView)
        val diseaseNameTextView = view.findViewById<TextView>(R.id.diseaseNameTextView)
        val confidenceTextView = view.findViewById<TextView>(R.id.confidenceTextView)
        val descriptionTextView = view.findViewById<TextView>(R.id.descriptionTextView)
        val textLabel = view.findViewById<TextView>(R.id.textLabel)
        val detailButtonLayout = view.findViewById<LinearLayout>(R.id.detailButtonLayout)

        // 로고 투톤 색상 적용
        logoTop?.let {
            it.typeface = ResourcesCompat.getFont(requireContext(), R.font.quantico_bold)

            val logoString = "QcumbeR"
            val spannableString = SpannableString(logoString)

            val darkGreen = ContextCompat.getColor(requireContext(), R.color.dark_green)
            val primaryGreen = ContextCompat.getColor(requireContext(), R.color.primary_green)

            spannableString.setSpan(
                ForegroundColorSpan(darkGreen),
                0, 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                ForegroundColorSpan(primaryGreen),
                1, 6,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                ForegroundColorSpan(darkGreen),
                6, 7,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            it.text = spannableString
        }

        // 이미지 로드
        if (fromHistory && imageResId != -1) {
            resultImageView.setImageResource(imageResId)
        } else {
            imageUriString?.let {
                val imageUri = Uri.parse(it)
                loadAndRotateImage(imageUri, resultImageView)
            }
        }

        // 진단 결과 표시
        diseaseNameTextView.text = diseaseName
        confidenceTextView.text = "${confidence}%"

        // 병해에 따른 색상, 설명, 레이블, 버튼 변경
        when {
            diseaseName.contains("노균병") -> {
                val color = resources.getColor(R.color.disease_yellow, null)
                diseaseNameTextView.setTextColor(color)
                confidenceTextView.setTextColor(color)
                textLabel.text = "이 의심돼요"
                descriptionTextView.text = "잎 표면에 처음에는 퇴록한 부정형 반점이 생기고, 감염부위가 담황색을 띕니다."
                detailButtonLayout?.visibility = View.VISIBLE
            }
            diseaseName.contains("흰가루병") -> {
                val color = resources.getColor(R.color.disease_yellow, null)
                diseaseNameTextView.setTextColor(color)
                confidenceTextView.setTextColor(color)
                textLabel.text = "이 의심돼요"
                descriptionTextView.text = "잎에 3~5mm정도의 회색 균사체가 나타나다가 점차 잎 전체가 밀가루를 뿌린 것처럼 확대돼요."
                detailButtonLayout?.visibility = View.VISIBLE
            }
            diseaseName.contains("정상") -> {
                val color = resources.getColor(R.color.disease_blue, null)
                diseaseNameTextView.setTextColor(color)
                confidenceTextView.setTextColor(color)
                textLabel.text = "적인 잎입니다"
                descriptionTextView.text = "정상적인 생육입니다."
                detailButtonLayout?.visibility = View.GONE
            }
            else -> {
                Log.e(TAG, "알 수 없는 병명: $diseaseName (신뢰도: $confidence%)")

                val color = resources.getColor(R.color.gray, null)
                diseaseNameTextView.setTextColor(color)
                confidenceTextView.setTextColor(color)
                textLabel.text = ""
                descriptionTextView.text = "알 수 없는 진단 결과입니다.\n다시 촬영해주세요."
                detailButtonLayout?.visibility = View.GONE
            }
        }
    }

    private fun loadAndRotateImage(imageUri: Uri, imageView: ImageView) {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(imageUri)
            var bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val exifInputStream: InputStream? = requireContext().contentResolver.openInputStream(imageUri)
            exifInputStream?.let { stream ->
                val exif = ExifInterface(stream)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )

                val rotation = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }

                if (rotation != 0f) {
                    bitmap = rotateBitmap(bitmap, rotation)
                }

                stream.close()
            }

            imageView.setImageBitmap(bitmap)

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
}