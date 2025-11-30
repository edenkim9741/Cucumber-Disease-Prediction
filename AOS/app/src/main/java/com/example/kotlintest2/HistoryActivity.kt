package com.example.kotlintest2

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import com.google.android.material.bottomsheet.BottomSheetDialog

class HistoryActivity : AppCompatActivity() {

    private lateinit var backButton: Button
    private lateinit var sortButton: LinearLayout
    private lateinit var sortText: TextView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

    // 🔥 Chip → ImageView 로 변경
    private lateinit var filterNormal: ImageView
    private lateinit var filterDowny: ImageView
    private lateinit var filterPowdery: ImageView
    private lateinit var filterOod: ImageView

    // 🔥 체크 상태 Bool 로 관리
    private var normalOn = true
    private var downyOn = true
    private var powderyOn = true
    private var oodOn = true

    private lateinit var historyManager: HistoryManager

    private var allHistoryItems: List<HistoryItem> = emptyList()
    private var isNewestFirst = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        backButton = findViewById(R.id.backButton)
        sortButton = findViewById(R.id.sortButton)
        sortText = findViewById(R.id.sortText)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)

        // 🔥 이미지 필터 버튼 연결
        filterNormal = findViewById(R.id.filterNormal)
        filterDowny = findViewById(R.id.filterDowny)
        filterPowdery = findViewById(R.id.filterPowdery)
        filterOod = findViewById(R.id.filterOod)

        historyManager = HistoryManager(this)

        setupRecyclerView()
        cleanupInvalidItemsInBackground()

        backButton.setOnClickListener { finish() }
        sortButton.setOnClickListener { showSortDialog() }

        // 🔥 클릭 → ON/OFF 토글 + 이미지 변경 + 필터반영
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
            // 🔹 최신순 (날짜 최신 → id 큰 순)
            filteredItems.sortedWith(
                compareByDescending<HistoryItem> { it.date }
                    .thenByDescending { it.id }
            )
        } else {
            // 🔹 오래된순 (날짜 오래됨 → id 작은 순)
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

        // 현재 상태 UI 표시
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