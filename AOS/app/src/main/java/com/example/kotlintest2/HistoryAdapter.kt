package com.example.kotlintest2

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private val historyItems: List<HistoryItem>,
    private val onItemClick: (HistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    // 날짜별로 그룹화
    private val groupedItems: List<Pair<String, List<HistoryItem>>> by lazy {
        historyItems.groupBy { it.date }
            .map { (date, items) -> date to items }
            .sortedByDescending { it.first }
    }

    // 플랫 리스트 (헤더 + 아이템)
    private val flatList: List<Any> by lazy {
        groupedItems.flatMap { (date, items) ->
            listOf(date) + items
        }
    }

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (flatList[position] is String) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history_header, parent, false)
            HistoryViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history, parent, false)
            HistoryViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        when (val item = flatList[position]) {
            is String -> holder.bindHeader(item)
            is HistoryItem -> holder.bind(item, onItemClick)
        }
    }

    override fun getItemCount(): Int = flatList.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView? = itemView.findViewById(R.id.dateTextView)
        private val thumbnailImageView: ImageView? = itemView.findViewById(R.id.thumbnailImageView)
        private val diseaseNameTextView: TextView? = itemView.findViewById(R.id.diseaseNameTextView)
        private val confidenceTextView: TextView? = itemView.findViewById(R.id.confidenceTextView)

        fun bindHeader(date: String) {
            dateTextView?.text = date
        }

        fun bind(historyItem: HistoryItem, onItemClick: (HistoryItem) -> Unit) {
            thumbnailImageView?.setImageResource(historyItem.imageResId)
            diseaseNameTextView?.text = historyItem.diseaseName
            confidenceTextView?.text = "${historyItem.confidence}%"

            // 병변에 따른 색상 설정
            val textColor = when {
                historyItem.diseaseName.contains("정상") -> Color.parseColor("#4A90E2")
                historyItem.diseaseName.contains("노균") -> Color.parseColor("#F9C74F")
                historyItem.diseaseName.contains("흰가루") -> Color.parseColor("#FF6B6B")
                else -> Color.parseColor("#8BC34A")
            }

            diseaseNameTextView?.setTextColor(textColor)
            confidenceTextView?.setTextColor(textColor)

            itemView.setOnClickListener {
                onItemClick(historyItem)
            }
        }
    }
}