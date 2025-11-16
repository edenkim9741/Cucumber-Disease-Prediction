package com.example.kotlintest2

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment

class DetailFragment : Fragment() {

    companion object {
        private const val TAG = "DetailFragment"
        private const val ARG_DISEASE_NAME = "diseaseName"

        fun newInstance(diseaseName: String): DetailFragment {
            val fragment = DetailFragment()
            val args = Bundle()
            args.putString(ARG_DISEASE_NAME, diseaseName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val diseaseName = arguments?.getString(ARG_DISEASE_NAME)!!

        val layoutResId = when {
            diseaseName.contains("노균병") -> R.layout.fragment_detail_downy
            diseaseName.contains("흰가루병") -> R.layout.fragment_detail_powdery
            else -> {
                Log.e(TAG, "예상 못한 병명: $diseaseName")
                R.layout.fragment_detail_downy
            }
        }

        return inflater.inflate(layoutResId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val diseaseName = arguments?.getString(ARG_DISEASE_NAME)!!

        // 로고 투톤 색상 적용
        applyLogoStyle(view)

        // "이런 증상을 보여요" 부분 색상 적용
        applySymptomTitleStyle(view)

        // "이렇게 대처해요" 부분 색상 적용
        applyActionTitleStyle(view)

        // 병명에 따라 다른 텍스트 스타일 적용
        if (diseaseName.contains("흰가루병")) {
            applyPowderyMildewStyles(view)
        } else if (diseaseName.contains("노균병")) {
            applyDownyMildewStyles(view)
        }
    }

    private fun applyLogoStyle(view: View) {
        val logoDetail = view.findViewById<TextView>(R.id.logoDetail)
        logoDetail?.let {
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
    }

    private fun applySymptomTitleStyle(view: View) {
        val symptomTitle = view.findViewById<TextView>(R.id.symptomTitle)
        symptomTitle?.let {
            val titleString = "이런 증상을 보여요"
            val spannableString = SpannableString(titleString)

            val darkGreen = ContextCompat.getColor(requireContext(), R.color.dark_green)
            val symptomDark = ContextCompat.getColor(requireContext(), R.color.symptom_dark)

            spannableString.setSpan(
                ForegroundColorSpan(darkGreen),
                0, 3,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                ForegroundColorSpan(symptomDark),
                3, 5,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                ForegroundColorSpan(darkGreen),
                5, 10,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            it.text = spannableString
        }
    }

    private fun applyActionTitleStyle(view: View) {
        val actionTitle = view.findViewById<TextView>(R.id.actionTitle)
        actionTitle?.let {
            val titleString = "이렇게 대처해요"
            val spannableString = SpannableString(titleString)

            val darkGreen = ContextCompat.getColor(requireContext(), R.color.dark_green)
            val symptomDark = ContextCompat.getColor(requireContext(), R.color.symptom_dark)

            spannableString.setSpan(
                ForegroundColorSpan(darkGreen),
                0, 4,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                ForegroundColorSpan(symptomDark),
                4, 6,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                ForegroundColorSpan(darkGreen),
                6, 8,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            it.text = spannableString
        }
    }

    // 흰가루병 텍스트 스타일
    private fun applyPowderyMildewStyles(view: View) {
        val pretendardLight = ResourcesCompat.getFont(requireContext(), R.font.pretendard_light)
        val pretendardBold = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)

        // 증상 설명 키워드
        applyTextStyleFromXml(view.findViewById(R.id.symptomText1),
            listOf(), pretendardLight, pretendardBold)

        applyTextStyleFromXml(view.findViewById(R.id.symptomText2),
            listOf("흰가루가 밀생"), pretendardLight, pretendardBold)

        applyTextStyleFromXml(view.findViewById(R.id.symptomText3),
            listOf("회백색", "흑색의 소립점(자낭각)이 형성"), pretendardLight, pretendardBold)

        applyTextStyleFromXml(view.findViewById(R.id.symptomText4),
            listOf("고사"), pretendardLight, pretendardBold)

        // 대처 방법 키워드
        applyTextStyleFromXml(view.findViewById(R.id.actionText1),
            listOf("제거하여 태워요."), pretendardLight, pretendardBold)

        applyTextStyleFromXml(view.findViewById(R.id.actionText2),
            listOf("통풍"), pretendardLight, pretendardBold)

        applyTextStyleFromXml(view.findViewById(R.id.actionText3),
            listOf("과용을 피해"), pretendardLight, pretendardBold)

        applyTextStyleFromXml(view.findViewById(R.id.actionText4),
            listOf("등록약제를 이용"), pretendardLight, pretendardBold)
    }

    // 노균병 텍스트 스타일
    private fun applyDownyMildewStyles(view: View) {
        val pretendardLight = ResourcesCompat.getFont(requireContext(), R.font.pretendard_light)
        val pretendardBold = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)

        // 증상 설명 키워드
        applyTextStyleFromXml(view.findViewById(R.id.symptomText1),
            listOf("생육 중기 및 후기"), pretendardLight, pretendardBold)

        applyTextStyleFromXml(view.findViewById(R.id.symptomText2),
            listOf("작은 부정형 반점", "엷은 황색"), pretendardLight, pretendardBold)

        applyTextStyleFromXml(view.findViewById(R.id.symptomText3),
            listOf("아랫 잎에서 먼저 발생", "잎이 말라죽어요."), pretendardLight, pretendardBold)

        applyTextStyleFromXml(view.findViewById(R.id.symptomText4),
            listOf("황갈색을 띠어요."), pretendardLight, pretendardBold)

        applyTextStyleFromXml(view.findViewById(R.id.symptomText5),
            listOf("이슬처럼 보이는 곰팡이가 다량 형성되어 회백색"), pretendardLight, pretendardBold)

        // 대처 방법 키워드
        applyTextStyleFromXml(view.findViewById(R.id.actionText1),
            listOf("불에 태우거나 땅속 깊이 묻어요."), pretendardLight, pretendardBold)

        applyTextStyleFromXml(view.findViewById(R.id.actionText2),
            listOf("잎에 물방울이 장시간 맺혀 있지 않도록 관리"), pretendardLight, pretendardBold)

        applyTextStyleFromXml(view.findViewById(R.id.actionText3),
            listOf("토양이 과습하지 않도록해요."), pretendardLight, pretendardBold)
    }

    // XML의 텍스트를 읽어서 부분 폰트 적용
    private fun applyTextStyleFromXml(
        textView: TextView?,
        keywords: List<String>,
        lightFont: Typeface?,
        boldFont: Typeface?
    ) {
        textView?.let { tv ->
            val fullText = tv.text.toString()  // XML의 텍스트 그대로 사용

            // 기본 폰트를 Light로 설정
            tv.typeface = lightFont

            // Bold 키워드가 있으면 SpannableString 적용
            if (keywords.isNotEmpty() && boldFont != null) {
                val spannable = SpannableString(fullText)

                keywords.forEach { keyword ->
                    var startIndex = fullText.indexOf(keyword)
                    while (startIndex != -1) {
                        spannable.setSpan(
                            CustomTypefaceSpan(boldFont),
                            startIndex,
                            startIndex + keyword.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        startIndex = fullText.indexOf(keyword, startIndex + keyword.length)
                    }
                }

                tv.text = spannable
            }
        }
    }

    // CustomTypefaceSpan 클래스
    private class CustomTypefaceSpan(private val typeface: Typeface) : android.text.style.MetricAffectingSpan() {
        override fun updateDrawState(paint: android.text.TextPaint) {
            paint.typeface = typeface
        }

        override fun updateMeasureState(paint: android.text.TextPaint) {
            paint.typeface = typeface
        }
    }
}