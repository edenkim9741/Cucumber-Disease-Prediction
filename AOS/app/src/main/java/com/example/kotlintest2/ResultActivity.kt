package com.example.kotlintest2

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class ResultActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        backButton = findViewById(R.id.backButton)
        viewPager = findViewById(R.id.viewPager)

        // Intent로 데이터 받기 (null 허용)
        val imageUriString = intent.getStringExtra("imageUri")
        val imageResId = intent.getIntExtra("imageResId", -1)
        val diseaseName = intent.getStringExtra("diseaseName")
        val confidence = intent.getIntExtra("confidence", -1)
        val fromHistory = intent.getBooleanExtra("fromHistory", false)

        // 데이터 유효성 검사 (에러 처리)
        if (diseaseName == null || confidence == -1) {
            // 필수 데이터가 없으면 에러 메시지 표시하고 종료
            Toast.makeText(
                this,
                "진단 결과를 불러올 수 없습니다.\n다시 촬영해주세요.",
                Toast.LENGTH_LONG
            ).show()
            finish()  // Activity 종료 (이전 화면으로 돌아감)
            return    // onCreate 종료
        }

        // 이미지 데이터도 검증 (선택적)
        if (!fromHistory && imageUriString == null) {
            Toast.makeText(
                this,
                "이미지를 불러올 수 없습니다.",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        // 모든 데이터가 정상이면 ViewPager 설정
        val adapter = ResultPagerAdapter(
            this,
            imageUriString,
            imageResId,
            diseaseName,  // null 아님이 보장됨
            confidence,   // -1 아님이 보장됨
            fromHistory
        )
        viewPager.adapter = adapter
        viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL

        // 뒤로가기 버튼
        backButton.setOnClickListener {
            finish()
        }
    }

    private inner class ResultPagerAdapter(
        fa: FragmentActivity,
        private val imageUri: String?,
        private val imageResId: Int,
        private val diseaseName: String,
        private val confidence: Int,
        private val fromHistory: Boolean
    ) : FragmentStateAdapter(fa) {

        // 총 페이지 수 (정상이면 1개, 병해면 2개)
        override fun getItemCount(): Int {
            return if (diseaseName.contains("정상")) {
                1  // 정상이면 ResultFragment만
            } else {
                2  // 병해면 ResultFragment + DetailFragment
            }
        }

        // 각 위치에 어떤 Fragment 보여줄지
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                // 결과 화면
                0 -> ResultFragment.newInstance(
                    imageUri,
                    imageResId,
                    diseaseName,
                    confidence,
                    fromHistory
                )
                // 상세 화면
                1 -> DetailFragment.newInstance(diseaseName)

                else -> ResultFragment.newInstance(
                    imageUri,
                    imageResId,
                    diseaseName,
                    confidence,
                    fromHistory
                )
            }
        }
    }
}