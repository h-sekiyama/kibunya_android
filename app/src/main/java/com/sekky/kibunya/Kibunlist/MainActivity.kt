package com.sekky.kibunya.Kibunlist

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout.VERTICAL
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.KibunDetail.KibunDetailActivity
import com.sekky.kibunya.KibunInput.KibunInputActivity
import com.sekky.kibunya.Kibuns
import com.sekky.kibunya.Other.AddFamilyActivity
import com.sekky.kibunya.Other.FamilyAdapter
import com.sekky.kibunya.Other.OtherActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityMainBinding
import com.sekky.kibunya.databinding.ActivityViewFamilyListBinding
import kotlinx.android.synthetic.main.tab_layout.view.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    // 日記を表示している日付（Date型）
    private var showDiaryDate: Date = Date()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 招待から起動した場合はユーザーIDを取得し家族追加画面に遷移
        val uri: Uri? = this.intent.data
        if (uri != null) {
            val intent = Intent(this, AddFamilyActivity::class.java).apply {
                putExtra("userId", uri.toString().takeLast(28))
            }
            startActivity(intent)
        }

        init()
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

        containsMyUidDocument.get().addOnSuccessListener { resultMap ->
            if (resultMap.documents.size != 0) {    // 家族が1人もいない
                familyList = resultMap.documents[0].get("user_id") as List<String>
            }

            db.collection("kibuns").whereIn("user_id", familyList).whereEqualTo("date", SimpleDateFormat("YYYY年MM月dd日").format(showDiaryDate)).addSnapshotListener { resultsMap, exception ->
                val results = resultsMap?.toObjects(Kibuns::class.java)
                    ?.sortedByDescending { kibuns -> kibuns.date }

                if (results!!.isEmpty()) {  // この日は誰も日記を書いてない
                    binding.noDiaryLabel.visibility = View.VISIBLE
                } else {
                    binding.noDiaryLabel.visibility = View.GONE

                    val adapter = binding.kibunsList.adapter as KibunsAdapter
                    adapter.addItems(results)
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
                                }
                            startActivity(intent)
                        }
                    })
                }
            }
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