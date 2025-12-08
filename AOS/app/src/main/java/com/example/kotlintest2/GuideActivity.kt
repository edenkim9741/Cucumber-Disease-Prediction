package com.example.kotlintest2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs
import android.os.Handler
import android.os.Looper

class GuideActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var gestureDetector: GestureDetectorCompat
    private var isAutoTransitioning = false // 자동 전환 중인지 체크

    companion object {
        private const val PREFS_NAME = "QcumbeRPrefs"
        private const val KEY_FIRST_LAUNCH = "isFirstLaunch"
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
        private const val PAGE_TRANSITION_DELAY = 500L // 4페이지 표시 후 메인으로 가는 딜레이 (ms)

        // 최초 실행 여부 확인
        fun isFirstLaunch(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        }

        // 최초 실행 완료 표시
        fun setFirstLaunchComplete(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)

        viewPager = findViewById(R.id.guideViewPager)

        val adapter = GuideAdapter(this)
        viewPager.adapter = adapter

        // 스와이프 비활성화 (가이드 4, 5에서만 활성화)
        viewPager.isUserInputEnabled = false

        // 제스처 감지기 초기화 (왼쪽 스와이프 감지용)
        gestureDetector = GestureDetectorCompat(this, SwipeGestureListener())

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                // ⭐ 가이드 4번과 5번에서 스와이프 활성화
                viewPager.isUserInputEnabled = (position == 3 || position == 4)

                // ⭐ 5→4로 이동했을 때 (자동 전환 중일 때)
                if (position == 3 && isAutoTransitioning) {
                    isAutoTransitioning = false

                    // 잠시 후 메인 화면으로 이동
                    Handler(Looper.getMainLooper()).postDelayed({
                        finishGuide()
                    }, PAGE_TRANSITION_DELAY)
                }
            }
        })
    }

    // 터치 이벤트를 제스처 감지기로 전달
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // 가이드 5번 페이지일 때만 왼쪽 스와이프 감지 (메인으로 가기)
        if (viewPager.currentItem == 4) {
            gestureDetector.onTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    // 스와이프 제스처 리스너 (5페이지에서 스와이프 방향 감지)
    inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (viewPager.currentItem != 4) return false

            val diffX = e2.x - (e1?.x ?: 0f)
            val diffY = e2.y - (e1?.y ?: 0f)

            // 수평 스와이프가 수직보다 클 때만
            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

                    // ⭐ 오른쪽 스와이프 (diffX > 0) → 4페이지로 가고 자동 전환 플래그 설정
                    if (diffX > 0) {
                        isAutoTransitioning = true
                        // ViewPager2가 자동으로 4페이지로 이동 (자연스러운 애니메이션)
                        // 실제 이동은 ViewPager2의 스와이프 기능으로 처리됨
                        return false // ViewPager2가 처리하도록 false 반환
                    }
                    // ⭐ 왼쪽 스와이프 (diffX < 0) → 바로 메인으로 이동
                    else {
                        finishGuide()
                        return true
                    }
                }
            }
            return false
        }
    }

    // 가이드 2번: 볼륨 업, 가이드 3번: 볼륨 다운
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (viewPager.currentItem == 1) { // 가이드 2번 페이지
                    viewPager.setCurrentItem(2, true)
                }
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (viewPager.currentItem == 2) { // 가이드 3번 페이지
                    viewPager.setCurrentItem(3, true)
                }
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    // 가이드 1번: 버튼 클릭으로 다음
    fun onGuidePageClick() {
        val currentItem = viewPager.currentItem
        if (currentItem == 0) {
            viewPager.setCurrentItem(1, true) // 가이드 1 → 2
        }
    }

    // 가이드 완료 후 MainActivity로 이동
    fun finishGuide() {
        setFirstLaunchComplete(this)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private inner class GuideAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 5 //  5개 페이지

        override fun createFragment(position: Int): Fragment {
            return GuideFragment.newInstance(position)
        }
    }
}

// 가이드 이미지를 보여주는 Fragment
class GuideFragment : Fragment() {

    companion object {
        private const val ARG_PAGE = "page"

        fun newInstance(page: Int): GuideFragment {
            val fragment = GuideFragment()
            val args = Bundle()
            args.putInt(ARG_PAGE, page)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_guide, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val page = arguments?.getInt(ARG_PAGE) ?: 0
        val imageView = view.findViewById<ImageView>(R.id.guideImageView)

        // 페이지별 이미지 설정
        val imageRes = when (page) {
            0 -> R.drawable.guide1 // 가이드 1번 - 버튼
            1 -> R.drawable.guide2 // 가이드 2번 - 볼륨 올리기
            2 -> R.drawable.guide3 // 가이드 3번 - 볼륨 내리기
            3 -> R.drawable.guide4 // 가이드 4번 - 왼쪽 슬라이드
            4 -> R.drawable.guide5 // 가이드 5번 - 오른쪽 슬라이드
            else -> R.drawable.guide1
        }

        imageView.setImageResource(imageRes)

        // 가이드 1번만 클릭으로 다음
        if (page == 0) {
            view.setOnClickListener {
                (activity as? GuideActivity)?.onGuidePageClick()
            }
        }
    }
}