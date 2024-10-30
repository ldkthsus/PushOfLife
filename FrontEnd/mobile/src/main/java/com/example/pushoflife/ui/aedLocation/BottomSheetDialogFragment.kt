package com.example.pushoflife.ui.aedLocation

import android.graphics.Paint
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.TypefaceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.pushoflife.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.graphics.Typeface

class AEDDetailBottomSheet(
    private val address: String,
    private val place: String,
    private val location: String,
    private val number: String
) : BottomSheetDialogFragment() {

    // CustomTypefaceSpan 클래스 정의
    inner class CustomTypefaceSpan(private val customTypeface: Typeface) : TypefaceSpan("") {
        override fun updateDrawState(ds: TextPaint) {
            applyCustomTypeFace(ds, customTypeface)
        }

        override fun updateMeasureState(paint: TextPaint) {
            applyCustomTypeFace(paint, customTypeface)
        }

        private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
            paint.typeface = tf
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_TransparentBackgroundBottomSheet)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_aed_detail_bottom_sheet, container, false)

        view.findViewById<TextView>(R.id.addressTextView).text = address

        val placeTextView = view.findViewById<TextView>(R.id.placeTextView)
        placeTextView.text = place
        placeTextView.typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)

        // CustomTypefaceSpan을 사용하여 설치 위치 텍스트에 '설치 위치'만 bold 폰트 적용
        val boldTypeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)
        val locationTextView = view.findViewById<TextView>(R.id.locationTextView)
        val locationSpannable = SpannableString("설치위치 $location")
        boldTypeface?.let {
            locationSpannable.setSpan(
                CustomTypefaceSpan(it),
                0,
                5,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        locationTextView.text = locationSpannable

        // CustomTypefaceSpan을 사용하여 전화번호 텍스트에 '전화번호'만 bold 폰트 적용
        val numberTextView = view.findViewById<TextView>(R.id.numberTextView)
        val numberSpannable = SpannableString("전화번호 $number")
        boldTypeface?.let {
            numberSpannable.setSpan(
                CustomTypefaceSpan(it),
                0,
                5,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        numberTextView.text = numberSpannable

        return view
    }
}
