package com.example.kotlintest2

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import java.io.InputStream
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan

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

        // ⭐ 간단모드 확인
        val isSimpleMode = SimpleModeManager.isSimpleMode(requireContext())

        if (isSimpleMode) {
            // ⭐ 간단모드 UI 적용
            applySimpleModeUI(
                diseaseNameTextView,
                confidenceTextView,
                textLabel,
                descriptionTextView,
                detailButtonLayout,
                diseaseName,
                confidence
            )
        } else {
            // 일반모드 - 기존 코드
            applyNormalModeUI(
                diseaseNameTextView,
                confidenceTextView,
                textLabel,
                descriptionTextView,
                detailButtonLayout,
                diseaseName,
                confidence
            )
        }
    }

    // 일반모드 UI
    private fun applyNormalModeUI(
        diseaseNameTextView: TextView,
        confidenceTextView: TextView,
        textLabel: TextView,
        descriptionTextView: TextView,
        detailButtonLayout: LinearLayout?,
        diseaseName: String,
        confidence: Int
    ) {
        // 1. 폰트 준비
        val pretendardBold = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)

        // 2. [도우미 함수] % 기호만 작게 만드는 함수 (내부 선언)
        fun setConfidenceText(view: TextView, value: Int, color: Int) {
            val fullText = "$value%"
            val spannable = SpannableString(fullText)

            // % 기호(마지막 1글자)를 0.6배 크기로 설정 (일반모드는 글씨가 작으니 0.6f 추천)
            spannable.setSpan(
                RelativeSizeSpan(0.6f),
                fullText.length - 1,
                fullText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            view.text = spannable
            view.setTextColor(color)
            view.typeface = pretendardBold // 폰트도 여기서 한 번에 적용
        }

        // 3. 기본 텍스트 설정 (병 이름)
        diseaseNameTextView.text = diseaseName

        // 4. 공통 폰트 설정 (나머지 뷰들)
        diseaseNameTextView.typeface = pretendardBold
        textLabel.typeface = pretendardBold
        descriptionTextView.typeface = pretendardBold

        // 5. 조건별 UI 적용
        when {
            diseaseName.contains("노균병") -> {
                val color = resources.getColor(R.color.disease_yellow, null)
                diseaseNameTextView.setTextColor(color)

                // ⭐ 함수 사용 (% 작게 + 색상 적용)
                setConfidenceText(confidenceTextView, confidence, color)

                textLabel.text = "이 의심돼요"
                descriptionTextView.text = "잎 표면에 처음에는 퇴록한 부정형 반점이 생기고, 감염부위가 담황색을 띕니다."
                detailButtonLayout?.visibility = View.VISIBLE
            }

            diseaseName.contains("흰가루병") -> {
                val color = resources.getColor(R.color.disease_yellow, null)
                diseaseNameTextView.setTextColor(color)

                // ⭐ 함수 사용
                setConfidenceText(confidenceTextView, confidence, color)

                textLabel.text = "이 의심돼요"
                descriptionTextView.text = "잎에 3~5mm정도의 회색 균사체가 나타나다가 점차 잎 전체가 밀가루를 뿌린 것처럼 확대돼요."
                detailButtonLayout?.visibility = View.VISIBLE
            }

            diseaseName.contains("정상") -> {
                val color = resources.getColor(R.color.disease_blue, null)
                diseaseNameTextView.setTextColor(color)

                // ⭐ 함수 사용
                setConfidenceText(confidenceTextView, confidence, color)

                textLabel.text = "적인 잎입니다"
                textLabel.setTextColor(resources.getColor(android.R.color.black, null))
                descriptionTextView.text = "정상적인 생육입니다."
                detailButtonLayout?.visibility = View.GONE
            }

            diseaseName.contains("병변") -> {
                val color = resources.getColor(R.color.disease_yellow, null)
                diseaseNameTextView.setTextColor(color)
                diseaseNameTextView.text = "기타 질병"

                // ⭐ 함수 사용
                setConfidenceText(confidenceTextView, confidence, color)

                textLabel.text = "이 의심돼요"
                descriptionTextView.text = "오이 잎이 질병에 노출된 상태로 보여요."
                detailButtonLayout?.visibility = View.GONE
            }

            diseaseName.equals("ood", ignoreCase = true) -> {
                Log.e(TAG, "OOD (신뢰도: $confidence%)")

                diseaseNameTextView.text = "" // 이름 없음

                val color = resources.getColor(R.color.gray, null)

                // ⭐ 함수 사용
                setConfidenceText(confidenceTextView, confidence, color)

                textLabel.text = "인식하지 못했어요"
                descriptionTextView.text = "오이 잎이 인식되지 않았습니다. 오이 잎을 정확히 촬영해주세요."
                detailButtonLayout?.visibility = View.GONE
            }

            else -> {
                Log.e(TAG, "예상치 못한 병명: $diseaseName (신뢰도: $confidence%)")

                diseaseNameTextView.text = "" // 이름 없음

                val color = resources.getColor(R.color.gray, null)

                // ⭐ 함수 사용
                setConfidenceText(confidenceTextView, confidence, color)

                textLabel.text = "인식하지 못했어요"
                descriptionTextView.text = "오이 잎이 인식되지 않았습니다. 오이 잎을 정확히 촬영해주세요."
                detailButtonLayout?.visibility = View.GONE
            }
        }
    }
    // 간단모드 UI
    private fun applySimpleModeUI(
        diseaseNameTextView: TextView,
        confidenceTextView: TextView,
        textLabel: TextView,
        descriptionTextView: TextView,
        detailButtonLayout: LinearLayout?,
        diseaseName: String,
        confidence: Int
    ) {
        val pretendardBold = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)
        val moveUpValue = -20f
        val descMoveUpValue = -30f
        // 1. 공통 설정 (사이즈 및 폰트) - 한 번만 설정하면 됩니다!
        // 제목 (병 이름)
        diseaseNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 45f)
        diseaseNameTextView.typeface = pretendardBold
        diseaseNameTextView.translationY = moveUpValue

        // 퍼센트
        confidenceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60f)
        confidenceTextView.typeface = pretendardBold

        // 라벨 ("이 의심돼요" 등)
        textLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 45f)
        textLabel.typeface = pretendardBold
        textLabel.translationY = moveUpValue

        // 설명글 (줄간격 넓힘)
        descriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        descriptionTextView.typeface = pretendardBold
        descriptionTextView.setLineSpacing(0f, 1.2f) // 줄간격을 1.4배로 넓혀서 읽기 편하게
        descriptionTextView.translationY = descMoveUpValue
        // 상세 버튼 숨김
        detailButtonLayout?.visibility = View.GONE

       fun setConfidenceText(view: TextView, value: Int, color: Int) {
            val fullText = "$value%"
            val spannable = SpannableString(fullText)

            // % 기호(마지막 1글자)에만 0.5배 크기 적용
            spannable.setSpan(
                RelativeSizeSpan(0.5f), // 0.5f는 절반 크기. 0.6f 정도로 조절 가능
                fullText.length - 1,    // 시작 위치 (맨 끝 문자 바로 앞)
                fullText.length,        // 끝 위치
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            view.text = spannable
            view.setTextColor(color)
        }

        // 2. 내용 및 색상 설정 (병명에 따라 달라지는 부분만)
        when {
            diseaseName.contains("노균병") -> {
                val color = resources.getColor(R.color.disease_yellow, null)

                diseaseNameTextView.text = "노균병 "
                diseaseNameTextView.setTextColor(color)

                setConfidenceText(confidenceTextView, confidence, color)

                textLabel.text = "의심"
                descriptionTextView.text = "잎 표면에 처음에는 퇴록한 부정형 반점이 생기고, 감염부위가 담황색을 띕니다."
            }

            diseaseName.contains("흰가루병") -> {
                val color = resources.getColor(R.color.disease_yellow, null)

                diseaseNameTextView.text = "흰가루병 "
                diseaseNameTextView.setTextColor(color)

                setConfidenceText(confidenceTextView, confidence, color)

                textLabel.text = "의심"
                descriptionTextView.text = "잎에 3~5mm정도의 회색 균사체가 나타나다가 점차 잎 전체가 밀가루를 뿌린 것처럼 확대돼요."
            }

            diseaseName.contains("정상") -> {
                val color = resources.getColor(R.color.disease_blue, null)

                diseaseNameTextView.text = "정상 "
                diseaseNameTextView.setTextColor(color)

                setConfidenceText(confidenceTextView, confidence, color)

                textLabel.text = "잎"
                textLabel.setTextColor(resources.getColor(android.R.color.black, null))

                descriptionTextView.text = "정상적인 생육입니다."
            }

            diseaseName.contains("병변") -> {
                val color = resources.getColor(R.color.disease_yellow, null)

                diseaseNameTextView.text = "기타 질병 "
                diseaseNameTextView.setTextColor(color)

                setConfidenceText(confidenceTextView, confidence, color)

                textLabel.text = "의심"
                descriptionTextView.text = "오이 잎이 질병에 노출된 상태로 보여요."
            }

            // OOD (인식 불가) 또는 그 외
            else -> {
                val color = resources.getColor(R.color.gray, null)

                diseaseNameTextView.text = "" // 이름 없음

                setConfidenceText(confidenceTextView, confidence, color)

                textLabel.text = "인식 불가"
                descriptionTextView.text = "오이 잎이 인식되지 않았습니다.\n오이 잎을 정확히 촬영해주세요."
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