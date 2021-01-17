package com.sekky.kibunya.Other

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.KibunInput.KibunInputActivity
import com.sekky.kibunya.Kibunlist.MainActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityChangeProfileBinding
import kotlinx.android.synthetic.main.tab_layout.view.*
import com.google.firebase.storage.FirebaseStorage

class ChangeProfileActivity: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var beforeName: String = ""    // 変更前の名前

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        currentUser!!.reload()

        // CloudStorageを使う準備
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("profileIcon/QLlZHwR2Wlgm2pqlYNJt3VEx0Ti1.jpg")

        val binding: ActivityChangeProfileBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_change_profile)

        // 背景タップでキーボードを隠すための処理
        Functions.addBackgroundFocus(binding.background, this)

        // 名前入力ボックスの入力監視
        binding.nameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // nop
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateChangeProfileButtonEnable(binding)
            }

            override fun afterTextChanged(s: Editable?) {
                updateChangeProfileButtonEnable(binding)
            }
        })

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

        // 画像の初期値を設定
        Glide.with(this)
            .load(imageRef)
            .into(binding.profileImage)

        // 名前の初期値を設定
        beforeName = currentUser.displayName.toString()
        binding.nameInput.setText(beforeName)

        // 戻るボタン
        binding.leftButton.setOnClickListener {
            finish()
        }

        // 保存ボタンタップ
        binding.profileChangeButton.setOnClickListener {
            currentUser.apply {
                val profileUpdates : UserProfileChangeRequest = UserProfileChangeRequest.Builder()
                    .setDisplayName(
                        binding.nameInput.text.toString()
                    ).setPhotoUri(
                        Uri.parse("https://necobiyori.jp/wpsys/wp-content/uploads/2018/09/original-1-5-768x513.jpg")
                    ).build()
                updateProfile(profileUpdates).addOnCompleteListener(OnCompleteListener {
                    when (it.isSuccessful) {
                        true -> apply {
                            beforeName = binding.nameInput.text.toString()
                            binding.profileChangeCompleteLabel.visibility = View.VISIBLE
                            binding.profileChangeButton.isClickable = false
                            binding.profileChangeButton.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)
                        }
                        false -> Functions.showAlertOneButton(
                            this@ChangeProfileActivity,
                            "エラー",
                            "更新に失敗しました"
                        )
                    }
                })
            }
        }
    }

    // プロフィール変更ボタンの有効/無効の切り替え
    fun updateChangeProfileButtonEnable(binding: ActivityChangeProfileBinding) {
        if (binding.nameInput.text.count() in 1..16 && beforeName != binding.nameInput.text.toString()) {
            binding.profileChangeButton.isClickable = true
            binding.profileChangeButton.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_30dp)
        } else {
            binding.profileChangeButton.isClickable = false
            binding.profileChangeButton.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)
        }
    }
}