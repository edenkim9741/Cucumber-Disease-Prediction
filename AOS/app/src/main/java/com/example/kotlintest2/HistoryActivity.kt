package com.example.kotlintest2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import kotlin.collections.filter
import kotlin.jvm.java

class HistoryActivity : AppCompatActivity() {

    private lateinit var backButton: Button
    private lateinit var sortButton: LinearLayout
    private lateinit var sortText: TextView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

    private lateinit var filterNormal: Chip
    private lateinit var filterDowny: Chip
    private lateinit var filterPowdery: Chip

    private var allHistoryItems: List<HistoryItem> = emptyList()
    private var isNewestFirst = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // 뷰 초기화
        backButton = findViewById(R.id.backButton)
        sortButton = findViewById(R.id.sortButton)
        sortText = findViewById(R.id.sortText)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)

        filterNormal = findViewById(R.id.filterNormal)
        filterDowny = findViewById(R.id.filterDowny)
        filterPowdery = findViewById(R.id.filterPowdery)

        // 샘플 데이터 생성
        allHistoryItems = getSampleHistoryData()

        // RecyclerView 설정
        setupRecyclerView()

        // 버튼 리스너
        backButton.setOnClickListener { finish() }

        sortButton.setOnClickListener { showSortDialog() }

        // 필터 리스너
        filterNormal.setOnCheckedChangeListener { _, _ -> applyFilters() }
        filterDowny.setOnCheckedChangeListener { _, _ -> applyFilters() }
        filterPowdery.setOnCheckedChangeListener { _, _ -> applyFilters() }

        // 초기 필터 적용
        applyFilters()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(emptyList()) { historyItem ->
            navigateToResult(historyItem)
        }

        val gridLayoutManager = GridLayoutManager(this, 3)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (historyAdapter.getItemViewType(position)) {
                    HistoryAdapter.VIEW_TYPE_DATE -> 3 // 날짜는 전체 너비
                    else -> 1 // 이미지는 1칸
                }
            }
        }

        historyRecyclerView.apply {
            layoutManager = gridLayoutManager
            adapter = historyAdapter
            setHasFixedSize(false)
        }
    }

    private fun applyFilters() {
        // 필터링
        val filteredItems = allHistoryItems.filter { item ->
            when {
                item.diseaseName.contains("정상") && filterNormal.isChecked -> true
                item.diseaseName.contains("노균") && filterDowny.isChecked -> true
                item.diseaseName.contains("흰가루") && filterPowdery.isChecked -> true
                else -> false
            }
        }

        // 정렬
        val sortedItems = if (isNewestFirst) {
            // 최신순: 날짜 내림차순
            filteredItems.sortedWith(compareByDescending<HistoryItem> { it.date }.thenByDescending { it.id })
        } else {
            // 오래된 순: 날짜 오름차순
            filteredItems.sortedWith(compareBy<HistoryItem> { it.date }.thenBy { it.id })
        }

        historyAdapter.updateData(sortedItems)
    }

    private fun showSortDialog() {
        val options = arrayOf("최신순", "오래된 순")
        val checkedItem = if (isNewestFirst) 0 else 1

        AlertDialog.Builder(this)
            .setTitle("정렬")
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                isNewestFirst = (which == 0)
                sortText.text = options[which]
                applyFilters()
                dialog.dismiss()
            }
            .show()
    }

    private fun getSampleHistoryData(): List<HistoryItem> {
        return listOf(
            HistoryItem(1, R.drawable.sample_normal1, "정상", 97, "2025.08.17"),
            HistoryItem(2, R.drawable.sample_normal2, "정상", 93, "2025.08.17"),
            HistoryItem(3, R.drawable.sample_normal3, "정상", 91, "2025.08.10"),
            HistoryItem(5, R.drawable.sample_abnormal_n1, "노균", 91, "2025.08.17"),
            HistoryItem(6, R.drawable.sample_abnormal_n2, "노균", 88, "2025.08.10"),
            HistoryItem(7, R.drawable.sample_abnormal_n3, "노균", 96, "2025.08.10"),
            HistoryItem(8, R.drawable.sample_abnormal_w1, "흰가루", 91, "2025.08.09"),
            HistoryItem(9, R.drawable.sample_abnormal_w2, "흰가루", 91, "2025.08.09"),
        )
    }

    private fun navigateToResult(historyItem: HistoryItem) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("imageResId", historyItem.imageResId)
        intent.putExtra("diseaseName", historyItem.diseaseName)
        intent.putExtra("confidence", historyItem.confidence)
        intent.putExtra("fromHistory", true)
        startActivity(intent)
    }
}