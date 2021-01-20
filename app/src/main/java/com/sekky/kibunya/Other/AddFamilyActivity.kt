package com.sekky.kibunya.Other

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.Families
import com.sekky.kibunya.KibunInput.KibunInputActivity
import com.sekky.kibunya.Kibunlist.MainActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.Users
import com.sekky.kibunya.databinding.ActivityAddFamilyBinding
import kotlinx.android.synthetic.main.tab_layout.view.*
import java.io.ByteArrayOutputStream

class AddFamilyActivity: AppCompatActivity() {

    private val binding: ActivityAddFamilyBinding by lazy {
        DataBindingUtil.setContentView<ActivityAddFamilyBinding>(this, R.layout.activity_add_family)
    }

    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        // 招待からの起動の場合はuserIdを受け取る
        val userId: String? = intent.getStringExtra("userId")
        if (userId != null) {
            binding.userIdInput.setText(userId)
            searchUser(user!!)
        }

        // 背景タップでキーボードを隠すための処理
        Functions.addBackgroundFocus(binding.background, this)

        // 名前入力ボックスの入力監視
        binding.userIdInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // nop
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSearchButtonEnable()
            }

            override fun afterTextChanged(s: Editable?) {
                updateSearchButtonEnable()
            }
        })

        // 検索ボタンタップ
        binding.searchButton.setOnClickListener {
            searchUser(user!!)
        }

        // 家族追加ボタンタップ
        binding.addFamilyButton.setOnClickListener {
            val db: FirebaseFirestore = FirebaseFirestore.getInstance()

            // 自分と相手がまだ誰とも家族になって無い場合
            db.collection("families").document().set(
                Families(listOf(userId, user!!.uid))
            ).addOnSuccessListener {
                // 追加したユーザー名を表示
                binding.newFamilyName.setText(binding.searchedUserName.text)
                binding.addFamilyCompleteLabel.visibility = View.VISIBLE
                // 家族追加ボタンをdisabledにする
                binding.addFamilyButton.isClickable = false
                binding.addFamilyButton.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)
                updateSearchButtonEnable()
            }.addOnFailureListener {
                Functions.showAlertOneButton(this@AddFamilyActivity, "エラー", it.message.toString())
            }
        }

        // 自分のユーザーIDを初期表示
        binding.myUserIdInput.setText(user?.uid)

        // 自分のIDを送るボタンタップ
        binding.sendMyUserIdButton.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_TEXT,
                    "家族の交換日記アプリ「家族ダイアリー」\n"
                            + user!!.displayName + "からの招待です。\n\n" +
                            "iPhoneの方は以下をタップ！\n" +
                            "kazokuDiary://login?id=" + binding.myUserIdInput.text.toString() + "\n\n" +
                            "Androidの方は以下をタップ！\n" +
                            "http://kazoku-diary?user-id=" + binding.myUserIdInput.text.toString() + "\n\n" +
                            "アプリをダウンロード\n" +
                            "iOS版：https://apps.apple.com/us/app/id1528947553\n\n" +
                            "Android版：https://hogehoge.com"
                )
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        // タブボタン処理
        binding.tabLayout.tab_button0.setImageResource(R.drawable.tab0_on)
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

        // 戻るボタン
        binding.leftButton.setOnClickListener {
            finish()
        }
    }

    private fun searchUser(user: FirebaseUser) {
        // 入力したのが自分のユーザーIDの場合
        if (binding.userIdInput.text.toString() == user.uid ?: "") {
            binding.searchedUserName.text = getString(R.string.my_own_user_id_label)
            binding.searchedUserName.visibility = View.VISIBLE
        } else {
            val docRef = db.collection("users").document(binding.userIdInput.text.toString())
            docRef.get()
                .addOnSuccessListener { document ->
                    // HITしたユーザー名を表示
                    binding.searchedUserName.text = document.get("name").toString()
                    binding.searchedUserName.visibility = View.VISIBLE
                    binding.addFamilyButton.isClickable = true
                    binding.addFamilyButton.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_30dp)
                }
        }
    }

    // プロフィール変更ボタンの有効/無効の切り替え
    private fun updateSearchButtonEnable() {
        if (binding.userIdInput.text.count() == 28) {
            binding.searchButton.isClickable = true
            binding.searchButton.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_30dp)
        } else {
            binding.searchButton.isClickable = false
            binding.searchButton.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)
        }
    }
}