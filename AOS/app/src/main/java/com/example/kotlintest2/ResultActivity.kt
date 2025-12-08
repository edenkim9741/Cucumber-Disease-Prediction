package com.example.kotlintest2

import android.os.Bundle
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class ResultActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2

    // 스와이프 제스처 감지기 추가
    private lateinit var gestureDetector: GestureDetectorCompat

    companion object {
        // 스와이프 임계값
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        viewPager = findViewById(R.id.viewPager)

        // 제스처 감지기 초기화
        gestureDetector = GestureDetectorCompat(this, SwipeGestureListener())

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
    }

    // 볼륨 다운 버튼으로 뒤로가기
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                // 뒤로가기 (CameraFragment로 돌아감)
                finish()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    // 터치 이벤트를 제스처 감지기로 전달
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    // 스와이프 제스처 리스너
    inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false

            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            // 수평 스와이프가 수직보다 클 때만
            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    // 오른쪽 스와이프 (diffX > 0) → 뒤로가기
                    if (diffX > 0) {
                        finish()
                        return true
                    }
                }
            }
            return false
        }
    }

    private inner class ResultPagerAdapter(
        private val fragmentActivity: FragmentActivity,
        private val imageUri: String?,
        private val imageResId: Int,
        private val diseaseName: String,
        private val confidence: Int,
        private val fromHistory: Boolean
    ) : FragmentStateAdapter(fragmentActivity) {

        // 이 페이지 수 (정상/OOD/병변이면 1개, 병해면 2개)
        // 간단모드일 때는 항상 1페이지만
        override fun getItemCount(): Int {
            // 간단모드 확인
            val isSimpleMode = SimpleModeManager.isSimpleMode(fragmentActivity)

            if (isSimpleMode) {
                return 1  // 간단모드: 항상 1페이지
            }

            // 일반모드: 기존 로직
            return if (diseaseName.contains("정상") ||
                diseaseName.equals("ood", ignoreCase = true) ||
                diseaseName.contains("병변")) {
                1  // 정상, OOD, 병변이면 ResultFragment만
            } else {
                2  // 노균병/흰가루병이면 ResultFragment + DetailFragment
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