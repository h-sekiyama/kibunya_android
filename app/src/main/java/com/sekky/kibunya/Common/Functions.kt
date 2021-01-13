package com.sekky.kibunya.Common

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.BindingAdapter
import androidx.databinding.ViewDataBinding
import com.google.firebase.Timestamp
import com.sekky.kibunya.KibunInput.KibunInputActivity
import com.sekky.kibunya.Kibunlist.MainActivity
import kotlinx.android.synthetic.main.tab_layout.view.*
import java.text.SimpleDateFormat
import java.util.*

class Functions {
    companion object {
        // タイムスタンプ型の時間を渡すとHH:mm形式の文字列で返す
        @SuppressLint("SimpleDateFormat")
        fun getTimeString(timeStamp: Timestamp): String {
            val date = timeStamp.toDate()
            val df = SimpleDateFormat("HH:mm")
            return df.format(date)
        }

        // 曜日付きの今日の日付を取得
        @SuppressLint("SimpleDateFormat")
        fun getTodayString(): String {
            val date: Date = Date()
            val df = SimpleDateFormat("YYYY/MM/dd")
            val calendar: Calendar = Calendar.getInstance()
            val day: Int = calendar.get(Calendar.DAY_OF_WEEK)
            var weekDay = ""
            when(day) {
                Calendar.SUNDAY -> weekDay = "日"
                Calendar.MONDAY -> weekDay = "月"
                Calendar.TUESDAY -> weekDay = "火"
                Calendar.WEDNESDAY -> weekDay = "水"
                Calendar.THURSDAY -> weekDay = "木"
                Calendar.FRIDAY -> weekDay = "金"
                Calendar.SATURDAY -> weekDay = "土"
            }
            return df.format(date) + "(${weekDay})"
        }
        // 背景がタッチされた際にフォーカスを移す処理
        @SuppressLint("ClickableViewAccessibility")
        fun addBackgroundFocus(v: View) {
            v.setOnTouchListener { view, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    /* Fragmentのレイアウトがタッチされた時に、Fragment全体ににフォーカスを移す */
                    view.requestFocus()
                }
                view?.onTouchEvent(event) ?: true
            }
        }
        // キーボードを隠す処理
        fun addKeyboardHide(context: Context, v: View) {
            v.setOnFocusChangeListener { view, hasFocus ->
                if (!hasFocus) {
                    /* 入力欄からフォーカスが外れたタイミングでキーボードを閉じる */
                    val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                }
            }
        }
    }
}