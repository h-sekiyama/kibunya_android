package com.sekky.kibunya.KibunInput

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.Kibunlist.MainActivity
import com.sekky.kibunya.Kibuns
import com.sekky.kibunya.Other.OtherActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityKibunInputBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.tab_layout.view.*
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class KibunInputActivity: AppCompatActivity() {

    private val binding: ActivityKibunInputBinding by lazy {
        DataBindingUtil.setContentView<ActivityKibunInputBinding>(this, R.layout.activity_kibun_input)
    }

    // 選択中の気分
    private var selectedKibun: Int? = null
    private var isExsistSendImage: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()
    }

    // 画像をギャラリーから読み込んで戻ってきた時
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        init()

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(resultData)
            if (resultData != null) {
                if (resultCode == RESULT_OK) {
                    Glide.with(this)
                        .load(result.uri)
                        .signature(ObjectKey(System.currentTimeMillis()))
                        .into(binding.kibunImageSelect)

                    isExsistSendImage = true
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    val error = result.error
                    Functions.showAlertOneButton(this, "エラー", error.toString())
                }
            }
        }
    }

    private fun init() {
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

        // 「これでおくる」ボタンタップ
        binding.kibunSendButton.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            val db: FirebaseFirestore = FirebaseFirestore.getInstance()
            val documentId: String = db.collection("kibuns").document().id
            // CloudStorageを使う準備
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("diary/${documentId}.jpg")

            // 添付画像があるか判定
            if (isExsistSendImage) {
                val bitmap = (binding.kibunImageSelect.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                val data = baos.toByteArray()
                val uploadTask = imageRef.putBytes(data)
                uploadTask.addOnFailureListener { e ->
                    Functions.showAlertOneButton(
                        this@KibunInputActivity,
                        "エラー",
                        e.message.toString()
                    )
                }.addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { url ->
                        sendDiary(user, db, documentId, url.toString())
                    }
                }
            } else {
                sendDiary(user, db, documentId)
            }
        }

        // ギャラリーから画像選択
        binding.kibunImageSelect.setOnClickListener {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(360, 270)
                .start(this)
        }

        // 日記を書くボタンの有効/無効切り替え
        updateSendButtonEnable()
    }

    // 日記の更新
    private fun sendDiary(user: FirebaseUser?, db: FirebaseFirestore, documentId: String, imageUrl: String = "") {
        db.collection("kibuns").document(documentId).set(Kibuns(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY年MM月dd日")),
            documentId,
            imageUrl,
            selectedKibun,
            user!!.displayName,
            binding.kibunEditText.text.toString(),
            com.google.firebase.Timestamp.now(),
            user.uid
        )).addOnSuccessListener {
            binding.sendComplete.visibility = View.VISIBLE
            binding.kibunEditText.text.clear()
            binding.kibunSendButton.isClickable = false
            binding.kibunSendButton.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)
            binding.kibunImageSelect.setImageResource(R.drawable.diary_image_icon)
        }.addOnFailureListener {
            Log.d("error", it.toString())
        }
    }

    // 送信ボタンの有効/無効の切り替え
    private fun updateSendButtonEnable() {
        if (binding.kibunEditText.text.count() in 1..300 && selectedKibun != null) {
            binding.kibunSendButton.isClickable = true
            binding.kibunSendButton.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_30dp)
        } else {
            binding.kibunSendButton.isClickable = false
            binding.kibunSendButton.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)
        }
    }

    // いずれかの気分アイコンをタップした時の処理
    private fun tapKibunIcon(kibun: Int) {
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