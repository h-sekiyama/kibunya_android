package com.sekky.kibunya.KibunInput

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.Kibunlist.MainActivity
import com.sekky.kibunya.Kibuns
import com.sekky.kibunya.Other.OtherActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityKibunInputBinding
import kotlinx.android.synthetic.main.tab_layout.view.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class KibunInputActivity: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private val binding: ActivityKibunInputBinding by lazy {
        DataBindingUtil.setContentView<ActivityKibunInputBinding>(this, R.layout.activity_kibun_input)
    }

    // 選択中の気分
    private var selectedKibun: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // タブ処理
        binding.tabLayout.tab_button1.setImageResource(R.drawable.tab1_on)
        binding.tabLayout.tab_button0.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        binding.tabLayout.tab_button1.setOnClickListener {
            val intent = Intent(this, KibunInputActivity::class.java)
            startActivity(intent)
        }
        binding.tabLayout.tab_button2.setOnClickListener {
            val intent = Intent(this, OtherActivity::class.java)
            startActivity(intent)
        }

        // 背景タップでキーボードを隠すための処理
        Functions.addBackgroundFocus(binding.background, this)

        // 「これでおくる」ボタンタップ
        binding.kibunSendButton.setOnClickListener {
            val user = auth.currentUser
            val db: FirebaseFirestore = FirebaseFirestore.getInstance()
            val documentId: String = db.collection("kibuns").document().id
            db.collection("kibuns").document(documentId).set(Kibuns(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY年MM月dd日")),
                documentId,
                "",
                selectedKibun,
                user!!.displayName,
                binding.kibunEditText.text.toString(),
                com.google.firebase.Timestamp.now(),
                ""
            )).addOnSuccessListener {
                binding.sendComplete.visibility = View.VISIBLE
                binding.kibunEditText.text.clear()
            }.addOnFailureListener {
                Log.d("error", it.toString())
            }
        }

        // 気分アイコンにタップイベントを付与
        binding.kibunButton0.setOnClickListener {
            tapKibunIcon(0)
        }
        binding.kibunButton1.setOnClickListener {
            tapKibunIcon(1)
        }
        binding.kibunButton2.setOnClickListener {
            tapKibunIcon(2)
        }
        binding.kibunButton3.setOnClickListener {
            tapKibunIcon(3)
        }
        binding.kibunButton4.setOnClickListener {
            tapKibunIcon(4)
        }

        // 日記本文ボックスの入力を監視
        binding.kibunEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.textCount.text = (300 - binding.kibunEditText.text.count()).toString()
                if (binding.kibunEditText.text.count() > 300) {
                    binding.textCount.setTextColor(Color.RED)
                    updateSendButtonEnable()
                } else {
                    binding.textCount.setTextColor(getColor(R.color.button_disabled))
                    updateSendButtonEnable()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                updateSendButtonEnable()
            }
        })

        updateSendButtonEnable()
    }

    // 送信ボタンの有効/無効の切り替え
    fun updateSendButtonEnable() {
        if (binding.kibunEditText.text.count() in 1..300 && selectedKibun != null) {
            binding.kibunSendButton.isClickable = true
            binding.kibunSendButton.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_30dp)
        } else {
            binding.kibunSendButton.isClickable = false
            binding.kibunSendButton.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)
        }
    }

    // いずれかの気分アイコンをタップした時の処理
    fun tapKibunIcon(kibun: Int) {
        when(kibun) {
            0 -> {
                binding.kibunButton0.setImageResource(R.drawable.kibun_icon0)
                binding.kibunButton1.setImageResource(R.drawable.kibun_icon1_off)
                binding.kibunButton2.setImageResource(R.drawable.kibun_icon2_off)
                binding.kibunButton3.setImageResource(R.drawable.kibun_icon3_off)
                binding.kibunButton4.setImageResource(R.drawable.kibun_icon4_off)
            }
            1 -> {
                binding.kibunButton0.setImageResource(R.drawable.kibun_icon0_off)
                binding.kibunButton1.setImageResource(R.drawable.kibun_icon1)
                binding.kibunButton2.setImageResource(R.drawable.kibun_icon2_off)
                binding.kibunButton3.setImageResource(R.drawable.kibun_icon3_off)
                binding.kibunButton4.setImageResource(R.drawable.kibun_icon4_off)
            }
            2 -> {
                binding.kibunButton0.setImageResource(R.drawable.kibun_icon0_off)
                binding.kibunButton1.setImageResource(R.drawable.kibun_icon1_off)
                binding.kibunButton2.setImageResource(R.drawable.kibun_icon2)
                binding.kibunButton3.setImageResource(R.drawable.kibun_icon3_off)
                binding.kibunButton4.setImageResource(R.drawable.kibun_icon4_off)
            }
            3 -> {
                binding.kibunButton0.setImageResource(R.drawable.kibun_icon0_off)
                binding.kibunButton1.setImageResource(R.drawable.kibun_icon1_off)
                binding.kibunButton2.setImageResource(R.drawable.kibun_icon2_off)
                binding.kibunButton3.setImageResource(R.drawable.kibun_icon3)
                binding.kibunButton4.setImageResource(R.drawable.kibun_icon4_off)
            }
            4 -> {
                binding.kibunButton0.setImageResource(R.drawable.kibun_icon0_off)
                binding.kibunButton1.setImageResource(R.drawable.kibun_icon1_off)
                binding.kibunButton2.setImageResource(R.drawable.kibun_icon2_off)
                binding.kibunButton3.setImageResource(R.drawable.kibun_icon3_off)
                binding.kibunButton4.setImageResource(R.drawable.kibun_icon4)
            }
        }
        selectedKibun = kibun
        updateSendButtonEnable()
    }
}