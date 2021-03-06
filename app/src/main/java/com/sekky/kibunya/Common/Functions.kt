package com.sekky.kibunya.Common

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
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
        // タイムスタンプ型の時間を渡すとYYYY月MM月dd日 HH:mm形式の文字列で返す
        @SuppressLint("SimpleDateFormat")
        fun getYearMonthDayTimeString(timeStamp: Timestamp): String {
            val date = timeStamp.toDate()
            val df = SimpleDateFormat("YYYY/MM月dd日 HH:mm")
            return df.format(date)
        }
        // 曜日付きの日付を取得
        @SuppressLint("SimpleDateFormat")
        fun getTodayString(date: Date): String {
            val df = SimpleDateFormat("YYYY/MM/dd")
            val calendar: Calendar = Calendar.getInstance()
            calendar.time = date
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
        // 背景がタッチされた際にキーボードを隠す処理
        @SuppressLint("ClickableViewAccessibility")
        fun addBackgroundFocus(v: View, context: Context) {
            v.setOnTouchListener { view, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    /* Fragmentのレイアウトがタッチされた時に、Fragment全体ににフォーカスを移す */
                    val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                }
                view?.onTouchEvent(event) ?: true
            }
        }
        // 英語のエラーメッセージを元に日本語メッセージを返す
        fun getJapaneseErrorMessage(message: String): String {
            if (message.contains("email address is badly")) {
                return "不正なメールアドレスです"
            } else if (message.contains("given password is invalid")) {
                return "パスワードが脆弱すぎます"
            } else if (message.contains("email address is already in use")) {
                return "このメールアドレスは既に使われています"
            } else if (message.contains("The password is invalid")) {
                return "パスワードが違います"
            } else if (message.contains("The sms verification code used to create")) {
                return "認証コードが違います"
            } else if (message.contains("There is no user record")) {
                return "ユーザーが見つかりません"
            }
            return message
        }
        // アラートダイアログ（ボタン一つ）表示処理
        fun showAlertOneButton(context: Context, titleString: String, mainString: String) {
            AlertDialog.Builder(context)
                .setTitle(titleString)
                .setMessage(mainString)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}