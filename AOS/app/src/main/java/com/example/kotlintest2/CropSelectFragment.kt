package com.example.kotlintest2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class CropSelectFragment : Fragment() {

    // 현재 모드: "shoot"(촬영) or "history"(기록)
    private var mode = "shoot"
    private var selectedCrop: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_crop_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabShoot = view.findViewById<TextView>(R.id.tabShoot)
        val tabHistory = view.findViewById<TextView>(R.id.tabHistory)
        val titleText = view.findViewById<TextView>(R.id.titleText)
        val subtitleText = view.findViewById<TextView>(R.id.subtitleText)
        val actionButton = view.findViewById<Button>(R.id.actionButton)

        // 작물 버튼들
        val cropCucumber = view.findViewById<LinearLayout>(R.id.cropCucumber)
        val cropStrawberry = view.findViewById<LinearLayout>(R.id.cropStrawberry)
        val cropPaprika = view.findViewById<LinearLayout>(R.id.cropPaprika)
        val cropGrape = view.findViewById<LinearLayout>(R.id.cropGrape)
        val cropPepper = view.findViewById<LinearLayout>(R.id.cropPepper)
        val cropTomato = view.findViewById<LinearLayout>(R.id.cropTomato)

        fun updateUI() {
            if (mode == "shoot") {
                tabShoot.setTextColor(resources.getColor(R.color.dark_green, null))
                tabHistory.setTextColor(resources.getColor(android.R.color.darker_gray, null))
                titleText.text = "작물 선택"
                subtitleText.text = "병해 진단을 원하는 작물을 선택해주세요."
                actionButton.text = "촬영하기"
            } else {
                tabShoot.setTextColor(resources.getColor(android.R.color.darker_gray, null))
                tabHistory.setTextColor(resources.getColor(R.color.dark_green, null))
                titleText.text = "지난 기록 확인하기"
                subtitleText.text = "조회하고 싶은 작물을 선택해주세요."
                actionButton.text = "기록 보기"
            }
        }

        // 탭 전환
        tabShoot.setOnClickListener { mode = "shoot"; updateUI() }
        tabHistory.setOnClickListener { mode = "history"; updateUI() }

        // 작물 선택
        val cropMap = mapOf(
            cropCucumber to "오이",
            cropStrawberry to "딸기",
            cropPaprika to "파프리카",
            cropGrape to "포도",
            cropPepper to "고추",
            cropTomato to "토마토"
        )

        fun updateCropSelection(selected: LinearLayout) {
            cropMap.keys.forEach { it.alpha = 0.4f }
            selected.alpha = 1.0f
            selectedCrop = cropMap[selected]
        }

        cropMap.keys.forEach { cropView ->
            cropView.setOnClickListener { updateCropSelection(cropView) }
        }

        // 액션 버튼
        actionButton.setOnClickListener {
            if (mode == "shoot") {
                // 촬영 페이지로 이동
                (activity as? MainActivity)?.loadFragment(CameraFragment())
            } else {
                // 기록 보기 페이지로 이동
                startActivity(Intent(requireContext(), HistoryActivity::class.java))
            }
        }

        updateUI()
    }
}