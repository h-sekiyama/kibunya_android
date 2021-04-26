package com.sekky.kibunya.Kibunlist

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import android.widget.CalendarView
import android.widget.LinearLayout.VERTICAL
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nifcloud.mbaas.core.*
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.KibunDetail.KibunDetailActivity
import com.sekky.kibunya.KibunInput.KibunInputActivity
import com.sekky.kibunya.Kibuns
import com.sekky.kibunya.Other.AddFamilyActivity
import com.sekky.kibunya.Other.OtherActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.tab_layout.view.*
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    // 日記を表示している日付（Date型）
    private var showDiaryDate: Date = Date()

    private var installation = NCMBInstallation.getCurrentInstallation()

    override fun onResume() {
        super.onResume()

        // 招待から起動した場合はユーザーIDを取得し家族追加画面に遷移
        val uri: Uri? = this.intent.data
        if (uri != null) {
            val intent = Intent(this, AddFamilyActivity::class.java).apply {
                putExtra("userId", uri.toString().takeLast(28))
            }
            startActivity(intent)
        } else {

            // ニフクラに登録するdeviceTokenをPreferenceに保存しておく
            installation = NCMBInstallation.getCurrentInstallation()
            val dataStore: SharedPreferences =
                getSharedPreferences("DataStore", Context.MODE_PRIVATE)
            val editor = dataStore.edit()
            editor.putString(getString(R.string.device_Token_key), installation.deviceToken)
            editor.apply()

            // カレンダー非表示
            binding.popupLayout.visibility = View.INVISIBLE
            binding.overlay.visibility = View.INVISIBLE
            binding.calendarView.visibility = View.INVISIBLE

            init()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder(this)
                .setTitle("アプリ終了")
                .setMessage("アプリを閉じますか？")
                .setPositiveButton("OK") { _, _ ->
                    this.finishAndRemoveTask()
                    this.moveTaskToBack(true)
                }
                .setNegativeButton("キャンセル") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            return false
        }
        return false
    }

    override fun onBackPressed() {
        // nop
    }

    @SuppressLint("WrongConstant", "SimpleDateFormat")
    private fun init() {
        // プログレスバー用オーバーレイを表示
        binding.overlay.visibility = View.VISIBLE

        //ヘッダーの日付を表示
        binding.todayText.setText(Functions.getTodayString(showDiaryDate))

        binding.kibunsList.adapter = KibunsAdapter()
        binding.kibunsList.layoutManager = LinearLayoutManager(this, VERTICAL, false)

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        // まず家族リストを作る
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val familyRef = db.collection("families")
        val containsMyUidDocument = familyRef.whereArrayContains("user_id", user!!.uid)
        var familyList: List<String> = listOf(user.uid)
        var familyId = ""
        var diaryWrittenDays = arrayListOf<Calendar>()

        containsMyUidDocument.get().addOnSuccessListener { resultMap ->
            if (resultMap.documents.size != 0) {    // 家族がいる場合
                familyList = resultMap.documents[0].get("user_id") as List<String>
            }

            // 家族の誰かが日記を書いてる日付リストを作成
            db.collection("kibuns").whereIn("user_id", familyList).addSnapshotListener { resultsMap, exception ->
                resultsMap?.forEach {
                    diaryWrittenDays.add(Functions.getCalendarFromTimestamp(it.data["time"] as Timestamp))
                }
                // 日記を書いてるカレンダーの日にちにイベントアイコン表示
                val events = arrayListOf<EventDay>()
                diaryWrittenDays.forEach {
                    events.add(EventDay(it, R.drawable.event_icon, Color.parseColor("#228B22")))
                }
                calendarView.setEvents(events)
            }

            db.collection("kibuns").whereIn("user_id", familyList).whereEqualTo("date", SimpleDateFormat("YYYY年MM月dd日").format(showDiaryDate)).addSnapshotListener { resultsMap, exception ->
                if (resultMap.documents.size != 0) {
                    // 家族登録状況を元にニフクラにfamilyIdを登録
                    familyId = resultMap.documents[0].id
                    installation.channels = JSONArray("[${familyId}]")
                    installation.saveInBackground()
                }
                val results = resultsMap?.toObjects(Kibuns::class.java)
                    ?.sortedByDescending { kibuns -> kibuns.time } ?: return@addSnapshotListener

                if (results.isEmpty()) {  // この日は誰も日記を書いてない
                    binding.noDiaryLabel.visibility = View.VISIBLE
                } else {
                    binding.noDiaryLabel.visibility = View.GONE

                    val adapter = binding.kibunsList.adapter as KibunsAdapter
                    adapter.addItems(results)
                    // タップ時の処理
                    adapter.setOnItemClickListener(object : KibunsAdapter.OnItemClickListener {
                        override fun onClick(view: View, data: Kibuns) {
                            val intent =
                                Intent(this@MainActivity, KibunDetailActivity::class.java).apply {
                                    putExtra("text", data.text)
                                    putExtra("date", binding.todayText.text)
                                    putExtra("name", data.name)
                                    putExtra("kibun", data.kibun)
                                    putExtra("time", Functions.getTimeString(data.time))
                                    putExtra("image", data.image)
                                    putExtra("userId", data.user_id)
                                    putExtra("documentId", data.documentId)
                                }
                            startActivity(intent)
                        }
                    })
                    // 長押し時の処理
                    adapter.setOnItemLongClickLstener(object: KibunsAdapter.OnItemLongClickListener {
                        override fun onLongClick(view: View, data: Kibuns) {
                            if (data.user_id == user.uid) { // 自分の日記の場合
                                AlertDialog.Builder(this@MainActivity)
                                    .setTitle("日記削除")
                                    .setMessage("この日記を削除しますか？")
                                    .setPositiveButton("OK") { _, _ ->
                                        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
                                        db.collection("kibuns").document(data.documentId!!).delete()
                                        binding.kibunsList.adapter = KibunsAdapter()
                                    }
                                    .setNegativeButton("キャンセル") { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                    .show()
                            }
                        }
                    })
                }
            }
        }.addOnFailureListener {
            Functions.showAlertOneButton(this, "エラー", it.toString())
        }.addOnCompleteListener {
            // プログレスバー非表示
            binding.overlay.visibility = View.GONE
            binding.progressbar.visibility = View.GONE
        }

        // 1日戻るボタンタップ
        binding.leftButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = showDiaryDate
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            showDiaryDate = calendar.time
            binding.rightButton.isEnabled = true
            binding.rightButton.setBackgroundResource(R.drawable.arrow_r)
            init()
        }

        // 1日進むボタンタップ
        binding.rightButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = showDiaryDate
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            showDiaryDate = calendar.time
            if (SimpleDateFormat("dd-MM-yyyy").format(showDiaryDate) == SimpleDateFormat("dd-MM-yyyy").format(Date())) {  // 表示してる日付が今日ならそれ以上進めなくする
                binding.rightButton.isEnabled = false
                binding.rightButton.setBackgroundResource(R.drawable.arrow_r_off)
            }
            init()
        }

        // カレンダーボタンタップ
        binding.calendarButton.setOnClickListener {
            binding.popupLayout.visibility = View.VISIBLE
            binding.overlay.visibility = View.VISIBLE
            binding.calendarView.visibility = View.VISIBLE
        }

        // カレンダーの日付タップ
        binding.calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                showDiaryDate = eventDay.calendar.time
                if (SimpleDateFormat("dd-MM-yyyy").format(showDiaryDate) == SimpleDateFormat("dd-MM-yyyy").format(Date())) {  // 表示してる日付が今日ならそれ以上進めなくする
                    binding.rightButton.isEnabled = false
                    binding.rightButton.setBackgroundResource(R.drawable.arrow_r_off)
                }
                init()
                binding.popupLayout.visibility = View.GONE
                binding.overlay.visibility = View.GONE
                binding.calendarView.visibility = View.INVISIBLE
            }
        })

        // カレンダーエリア外タップ（カレンダーを閉じる）
        binding.overlay.setOnClickListener {
            binding.popupLayout.visibility = View.GONE
            binding.overlay.visibility = View.GONE
            binding.calendarView.visibility = View.INVISIBLE
        }

        // 選択中の日付をハイライト
        val calendar = Calendar.getInstance()
        calendar.set(Functions.getYearFromDate(showDiaryDate), Functions.getMontshFromDate(showDiaryDate), Functions.getDayFromDate(showDiaryDate))
        calendarView.setDate(calendar)

        // タブボタン処理
        binding.tabLayout.tab_button0.setImageResource(R.drawable.tab0_on)
        binding.tabLayout.tab_button0.setOnClickListener {
            val intent = Intent(this@MainActivity, MainActivity::class.java)
            startActivity(intent)
        }
        binding.tabLayout.tab_button1.setOnClickListener {
            val intent = Intent(this@MainActivity, KibunInputActivity::class.java)
            startActivity(intent)
        }
        binding.tabLayout.tab_button2.setOnClickListener {
            val intent = Intent(this@MainActivity, OtherActivity::class.java)
            startActivity(intent)
        }
    }
}