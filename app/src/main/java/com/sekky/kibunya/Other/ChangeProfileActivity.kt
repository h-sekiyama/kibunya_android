package com.sekky.kibunya.Other

import android.R.attr.data
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.KibunInput.KibunInputActivity
import com.sekky.kibunya.Kibunlist.MainActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityChangeProfileBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_change_profile.*
import kotlinx.android.synthetic.main.tab_layout.view.*
import java.io.ByteArrayOutputStream


class ChangeProfileActivity: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var beforeName: String = ""    // 変更前の名前
    private var isChangedProfIcon: Boolean = false  // 画像を更新しているか判定するフラグ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val binding: ActivityChangeProfileBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_change_profile)

        val currentUser = auth.currentUser
        // CloudStorageを使う準備
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("profileIcon/${currentUser!!.uid}.jpg")

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
        imageRef.getBytes(1000000).addOnSuccessListener {
            // 画像が存在すればそれを表示
            Glide.with(this)
                .load(imageRef)
                .into(binding.profileImage)
        }.addOnFailureListener {
            // 取得失敗したらデフォルト画像表示
            binding.profileImage.setImageResource(R.drawable.noimage)
        }


        // 名前の初期値を設定
        beforeName = currentUser!!.displayName.toString()
        binding.name = beforeName

        // 戻るボタン
        binding.leftButton.setOnClickListener {
            finish()
        }

        // ギャラリーから画像選択
        binding.profileImage.setOnClickListener {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this)
        }

        // 保存ボタンタップ
        binding.profileChangeButton.setOnClickListener {
            saveProfile(binding, currentUser, imageRef)
        }
    }

    // 画像をギャラリーから読み込んで戻ってきた時
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        val binding: ActivityChangeProfileBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_change_profile)

        binding.name = beforeName

        val currentUser = auth.currentUser
        // CloudStorageを使う準備
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("profileIcon/${currentUser!!.uid}.jpg")

        // 保存ボタンタップ
        binding.profileChangeButton.setOnClickListener {
            saveProfile(binding, currentUser, imageRef)
        }

        // ギャラリーから画像選択
        binding.profileImage.setOnClickListener {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this)
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(resultData)
            if (resultCode == RESULT_OK) {
                Glide.with(this)
                    .load(result.uri)
                    .into(binding.profileImage)

                // 保存ボタンをアクティブにする
                binding.profileChangeButton.isClickable = true
                binding.profileChangeButton.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_30dp)

                isChangedProfIcon = true
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
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
    }

    // プロフィール変更ボタンの有効/無効の切り替え
    private fun updateChangeProfileButtonEnable(binding: ActivityChangeProfileBinding) {
        if (binding.nameInput.text.count() in 1..16 && beforeName != binding.nameInput.text.toString()) {
            binding.profileChangeButton.isClickable = true
            binding.profileChangeButton.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_30dp)
        } else {
            binding.profileChangeButton.isClickable = false
            binding.profileChangeButton.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)
        }
    }

    private fun saveProfile(binding: ActivityChangeProfileBinding, currentUser: FirebaseUser, imageRef: StorageReference) {
        currentUser?.apply {
            val profileUpdates : UserProfileChangeRequest = UserProfileChangeRequest.Builder()
                .setDisplayName(
                    binding.nameInput.text.toString()
                ).build()
            updateProfile(profileUpdates).addOnCompleteListener { it ->
                when (it.isSuccessful) {
                    true -> apply {
                        beforeName = binding.nameInput.text.toString()
                        binding.profileChangeCompleteLabel.visibility = View.VISIBLE
                        binding.profileChangeButton.isClickable = false
                        binding.profileChangeButton.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)

                        if (isChangedProfIcon) {
                            val bitmap = (binding.profileImage.drawable as BitmapDrawable).bitmap
                            val baos = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                            val data = baos.toByteArray()
                            val uploadTask = imageRef.putBytes(data)
                            uploadTask.addOnFailureListener { e ->
                                Functions.showAlertOneButton(
                                    this@ChangeProfileActivity,
                                    "エラー",
                                    e.message.toString()
                                )
                            }.addOnSuccessListener { taskSnapshot ->
                                isChangedProfIcon = false
                            }
                        }
                    }
                    false -> Functions.showAlertOneButton(
                        this@ChangeProfileActivity,
                        "エラー",
                        "更新に失敗しました"
                    )
                }
            }
        }
    }
}