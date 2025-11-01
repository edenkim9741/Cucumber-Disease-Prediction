package com.example.kotlintest2

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private var cameraFragment: CameraFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_viewpager)

        viewPager = findViewById(R.id.viewPager)

        val adapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = adapter

        // 시작 페이지를 카메라(0번 인덱스)로 설정
        viewPager.setCurrentItem(0, false)
    }

    // 음량 버튼으로 사진 촬영
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                // 현재 페이지가 카메라 화면(0번 인덱스)일 때만 작동
                if (viewPager.currentItem == 0) {
                    cameraFragment?.takePhoto()
                }
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    // 카메라 Fragment를 저장해서 나중에 참조할 수 있도록
                    CameraFragment().also { cameraFragment = it }
                }
                1 -> MyInfoFragment()  // 내 정보 화면
                else -> CameraFragment()
            }
        }
    }
}