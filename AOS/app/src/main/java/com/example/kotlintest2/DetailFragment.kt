package com.example.kotlintest2

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment

class DetailFragment : Fragment() {

    companion object {
        private const val ARG_DISEASE_NAME = "diseaseName"

        fun newInstance(diseaseName: String): DetailFragment {
            val fragment = DetailFragment()
            val args = Bundle()
            args.putString(ARG_DISEASE_NAME, diseaseName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 로고 Quantico + 투톤 적용
        val logoText = view.findViewById<TextView>(R.id.logoDetail)
        logoText.typeface = ResourcesCompat.getFont(requireContext(), R.font.quantico_bold)

        val logoString = "QcumbeR"
        val spannableString = SpannableString(logoString)

        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.dark_green)),
            0, 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.dark_green)),
            6, 7,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        logoText.text = spannableString
    }
}