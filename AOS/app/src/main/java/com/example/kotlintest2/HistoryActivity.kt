package com.example.kotlintest2

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.math.abs

class HistoryActivity : AppCompatActivity() {

    private lateinit var backButton: Button
    private lateinit var sortButton: LinearLayout
    private lateinit var sortText: TextView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

    private lateinit var filterNormal: ImageView
    private lateinit var filterDowny: ImageView
    private lateinit var filterPowdery: ImageView
    private lateinit var filterOod: ImageView

    private var normalOn = true
    private var downyOn = true
    private var powderyOn = true
    private var oodOn = true

    private lateinit var historyManager: HistoryManager

    private var allHistoryItems: List<HistoryItem> = emptyList()
    private var isNewestFirst = true

    // ⭐ 스와이프 제스처 감지기 추가
    private lateinit var gestureDetector: GestureDetectorCompat

    companion object {
        // ⭐ 스와이프 임계값
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        backButton = findViewById(R.id.backButton)
        sortButton = findViewById(R.id.sortButton)
        sortText = findViewById(R.id.sortText)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)

        filterNormal = findViewById(R.id.filterNormal)
        filterDowny = findViewById(R.id.filterDowny)
        filterPowdery = findViewById(R.id.filterPowdery)
        filterOod = findViewById(R.id.filterOod)

        historyManager = HistoryManager(this)

        // ⭐ 제스처 감지기 초기화
        gestureDetector = GestureDetectorCompat(this, SwipeGestureListener())

        setupRecyclerView()
        cleanupInvalidItemsInBackground()

        backButton.setOnClickListener { finish() }
        sortButton.setOnClickListener { showSortDialog() }

        filterNormal.setOnClickListener {
            normalOn = !normalOn
            filterNormal.setImageResource(if (normalOn) R.drawable.filter_normal_on else R.drawable.filter_normal_off)
            applyFilters()
        }
        filterDowny.setOnClickListener {
            downyOn = !downyOn
            filterDowny.setImageResource(if (downyOn) R.drawable.filter_downy_on else R.drawable.filter_downy_off)
            applyFilters()
        }
        filterPowdery.setOnClickListener {
            powderyOn = !powderyOn
            filterPowdery.setImageResource(if (powderyOn) R.drawable.filter_powdery_on else R.drawable.filter_powdery_off)
            applyFilters()
        }
        filterOod.setOnClickListener {
            oodOn = !oodOn
            filterOod.setImageResource(if (oodOn) R.drawable.filter_ood_on else R.drawable.filter_ood_off)
            applyFilters()
        }
    }

    // ⭐ 터치 이벤트를 제스처 감지기로 전달
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    // ⭐ 스와이프 제스처 리스너
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

    override fun onResume() {
        super.onResume()
        loadHistoryData()
        applyFilters()
    }

    private fun cleanupInvalidItemsInBackground() {
        lifecycleScope.launch {
            val removedCount = withContext(Dispatchers.IO) {
                historyManager.cleanupInvalidItems()
            }

            loadHistoryData()
            applyFilters()

            if (removedCount > 0) {
                Toast.makeText(
                    this@HistoryActivity,
                    "삭제된 사진 ${removedCount}개가 기록에서 제거되었습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadHistoryData() {
        val realHistory = historyManager.getHistoryItems()
        val sampleHistory = getSampleHistoryData()
        allHistoryItems = realHistory + sampleHistory
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(emptyList()) { historyItem ->
            navigateToResult(historyItem)
        }

        val gridLayoutManager = GridLayoutManager(this, 3)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) =
                if (historyAdapter.getItemViewType(position) == HistoryAdapter.VIEW_TYPE_DATE) 3 else 1
        }

        historyRecyclerView.layoutManager = gridLayoutManager
        historyRecyclerView.adapter = historyAdapter
        historyRecyclerView.setHasFixedSize(false)
    }


    private fun applyFilters() {
        val filteredItems = allHistoryItems.filter { item ->
            (normalOn && item.diseaseName.contains("정상")) ||
                    (downyOn && item.diseaseName.contains("노균")) ||
                    (powderyOn && item.diseaseName.contains("흰가루")) ||
                    (oodOn && (item.diseaseName.contains("ood", true) || item.diseaseName.contains("알 수 없음")))
        }

        val sortedItems = if (isNewestFirst) {
            filteredItems.sortedWith(
                compareByDescending<HistoryItem> { it.date }
                    .thenByDescending { it.id }
            )
        } else {
            filteredItems.sortedWith(
                compareBy<HistoryItem> { it.date }
                    .thenBy { it.id }
            )
        }

        historyAdapter.updateData(sortedItems)
    }

    private fun showSortDialog() {

        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_sort, null)

        val newest = view.findViewById<TextView>(R.id.sortNewest)
        val oldest = view.findViewById<TextView>(R.id.sortOldest)

        newest.alpha = if (isNewestFirst) 1f else 0.4f
        oldest.alpha = if (!isNewestFirst) 1f else 0.4f

        newest.setOnClickListener {
            isNewestFirst = true
            sortText.text = "최신순"
            applyFilters()
            dialog.dismiss()
        }

        oldest.setOnClickListener {
            isNewestFirst = false
            sortText.text = "오래된 순"
            applyFilters()
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }


    private fun getSampleHistoryData() = listOf(
        HistoryItem(1001, R.drawable.sample_normal1, null, "정상", 97, "2025.08.17"),
        HistoryItem(1002, R.drawable.sample_normal2, null, "정상", 93, "2025.08.17"),
        HistoryItem(1003, R.drawable.sample_normal3, null, "정상", 91, "2025.08.10"),
        HistoryItem(1005, R.drawable.sample_abnormal_n1, null, "노균병", 91, "2025.08.17"),
        HistoryItem(1006, R.drawable.sample_abnormal_n2, null, "노균병", 88, "2025.08.10"),
        HistoryItem(1007, R.drawable.sample_abnormal_n3, null, "노균병", 96, "2025.08.10"),
        HistoryItem(1008, R.drawable.sample_abnormal_w1, null, "흰가루병", 91, "2025.08.09"),
        HistoryItem(1009, R.drawable.sample_abnormal_w2, null, "흰가루병", 91, "2025.08.09")
    )

    private fun navigateToResult(historyItem: HistoryItem) {
        val intent = Intent(this, ResultActivity::class.java)

        if (historyItem.imageUri != null)
            intent.putExtra("imageUri", historyItem.imageUri)
        else
            intent.putExtra("imageResId", historyItem.imageResId)

        intent.putExtra("diseaseName", historyItem.diseaseName)
        intent.putExtra("confidence", historyItem.confidence)
        intent.putExtra("fromHistory", true)
        startActivity(intent)
    }
}