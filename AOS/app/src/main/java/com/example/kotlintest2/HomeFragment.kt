package com.example.kotlintest2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 인사말
        val greetingText = view.findViewById<TextView>(R.id.greetingText)
        val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "농부"
        greetingText.text = "안녕하세요 ${userName}님!"

        // 우상단 사람 아이콘 → MyInfoActivity
        view.findViewById<ImageView>(R.id.profileIcon).setOnClickListener {
            startActivity(Intent(requireContext(), MyInfoActivity::class.java))
        }

        highlightToday(view)
    }

    private fun highlightToday(view: View) {
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val dayViews = listOf(
            view.findViewById<View>(R.id.dayMon),
            view.findViewById<View>(R.id.dayTue),
            view.findViewById<View>(R.id.dayWed),
            view.findViewById<View>(R.id.dayThu),
            view.findViewById<View>(R.id.dayFri),
            view.findViewById<View>(R.id.daySat),
            view.findViewById<View>(R.id.daySun)
        )
        val todayIndex = when (dayOfWeek) {
            Calendar.MONDAY -> 0; Calendar.TUESDAY -> 1; Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3; Calendar.FRIDAY -> 4; Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6; else -> -1
        }
        dayViews.forEachIndexed { index, dayView ->
            if (index == todayIndex) dayView.setBackgroundResource(R.drawable.day_highlight_background)
            else dayView.background = null
        }
    }
}