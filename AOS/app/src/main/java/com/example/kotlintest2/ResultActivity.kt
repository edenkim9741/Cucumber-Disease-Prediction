package com.example.kotlintest2

import android.os.Bundle
import android.widget.Button
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

        // Intent로 데이터 받기
        val imageUriString = intent.getStringExtra("imageUri")
        val imageResId = intent.getIntExtra("imageResId", -1)
        val diseaseName = intent.getStringExtra("diseaseName") ?: "노균병"
        val confidence = intent.getIntExtra("confidence", 92)
        val fromHistory = intent.getBooleanExtra("fromHistory", false)

        // ViewPager 설정
        val adapter = ResultPagerAdapter(
            this,
            imageUriString,
            imageResId,
            diseaseName,
            confidence,
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

        // 총 페이지 수  2개
        override fun getItemCount(): Int = 2
        // 각 위치에 어떤 Fragment 보여줄지(0 : 결과, 1 : 상세)
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ResultFragment.newInstance(imageUri, imageResId, diseaseName, confidence, fromHistory)
                1 -> DetailFragment.newInstance(diseaseName)
                else -> ResultFragment.newInstance(imageUri, imageResId, diseaseName, confidence, fromHistory)
            }
        }
    }
}