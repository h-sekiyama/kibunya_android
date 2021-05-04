package com.sekky.kibunya.KibunInput

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nifcloud.mbaas.core.NCMBInstallation
import com.nifcloud.mbaas.core.NCMBPush
import com.nifcloud.mbaas.core.NCMBQuery
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.Kibunlist.MainActivity
import com.sekky.kibunya.Kibuns
import com.sekky.kibunya.Other.OtherActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityKibunInputBinding
import kotlinx.android.synthetic.main.tab_layout.view.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class KibunInputActivity: AppCompatActivity() {

    private val binding: ActivityKibunInputBinding by lazy {
        DataBindingUtil.setContentView<ActivityKibunInputBinding>(
            this,
            R.layout.activity_kibun_input
        )
    }

    // 選択中の気分
    private var selectedKibun: Int? = null
    private var isExsistSendImage: Boolean = false

    // 家族ID
    private var familyId = ""

    // PUSH通知送信フラグ
    private var willSendPush = true

    // 日記画像のURI
    private var diaryImageUri: Uri? = null

    // 日記の投稿年月日（文字列）
    private var sendDiaryDateString: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY年MM月dd日"))

    // 日記の投稿日時のタイムスタンプ
    private var sendDiaryTime: Timestamp = Timestamp.now()

    // 日記の投稿年月日（Date型）
    private var sendDiaryDate: Date = Date()

    override fun onResume() {
        super.onResume()

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
                        .load(result?.uriContent)
                        .into(binding.kibunImageSelect)
                    diaryImageUri = result?.uriContent
                    isExsistSendImage = true
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    val error = result?.error
                    Functions.showAlertOneButton(this, "エラー", error.toString())
                }
            }
        }
    }

    private fun init() {
        // 家族IDの取得
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val familyRef = db.collection("families")
        val containsMyUidDocument = familyRef.whereArrayContains("user_id", user!!.uid)
        containsMyUidDocument.get().addOnSuccessListener { resultMap ->
            if (resultMap.documents.size != 0) {    // 家族が1人もいない
                familyId = resultMap.documents[0].id
            }
        }

        // PUSH通知のON/OFF
        binding.pushSwitch.setOnCheckedChangeListener { _, isChecked ->
            willSendPush = isChecked
        }

        // 下記途中の日記保存Preference
        val dataStore: SharedPreferences = getSharedPreferences("DataStore", Context.MODE_PRIVATE)
        val editor = dataStore.edit()
        val inputKibunText: String? = dataStore.getString(getString(R.string.family_diary_input_kibun_text), "")
        binding.kibunEditText.setText(inputKibunText.toString())

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
                // 下記途中の文章を保存
                editor.putString(getString(R.string.family_diary_input_kibun_text), binding.kibunEditText.text.toString())
                editor.apply()
            }

            override fun afterTextChanged(s: Editable?) {
                updateSendButtonEnable()
            }
        })

        // 「これでおくる」ボタンタップ
        binding.kibunSendButton.setOnClickListener {
            // プログレスバー表示
            binding.overlay.visibility = View.VISIBLE
            binding.progressbar.visibility = View.VISIBLE

            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            val db: FirebaseFirestore = FirebaseFirestore.getInstance()
            val documentId: String = db.collection("kibuns").document().id
            // CloudStorageを使う準備
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("diary/${documentId}.jpg")

            // 添付画像があるか判定
            if (isExsistSendImage) {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, diaryImageUri)
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
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

        // 日にち選択ダイアログ表示
        binding.changeDate.setOnClickListener {
            showDatePicker()
        }
    }

    // 日記の更新
    private fun sendDiary(
        user: FirebaseUser?,
        db: FirebaseFirestore,
        documentId: String,
        imageUrl: String = ""
    ) {
        db.collection("kibuns").document(documentId).set(
            Kibuns(
                sendDiaryDateString,
                documentId,
                imageUrl,
                selectedKibun,
                user!!.displayName,
                binding.kibunEditText.text.toString(),
                sendDiaryTime,
                user.uid
            )
        ).addOnSuccessListener {
            binding.sendComplete.visibility = View.VISIBLE
            binding.kibunEditText.text.clear()
            binding.kibunSendButton.isEnabled = false
            binding.kibunSendButton.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)
            binding.kibunImageSelect.setImageResource(R.drawable.diary_image_icon)
            // Preferenceに保存してるテキスト削除
            val dataStore: SharedPreferences = getSharedPreferences("DataStore", Context.MODE_PRIVATE)
            val editor = dataStore.edit()
            editor.putString(getString(R.string.family_diary_input_kibun_text), "")
            editor.apply()

            if (willSendPush) {
                if (familyId != "") {   // 家族がいる場合PUSH通知を送信する
                    val push = NCMBPush()
                    // i/A共通の設定
                    push.message = "${user.displayName}が日記を書きました"

                    // 送る対象を家族に限定
                    val installation = NCMBInstallation.getCurrentInstallation()
                    val query = NCMBQuery<NCMBInstallation>("installation")
                    query.whereContainedIn("channels", listOf(familyId))
                    query.whereNotEqualTo("deviceToken", installation.deviceToken) // 自分は除く
                    push.setSearchCondition(query)

                    // iOS用の設定
                    push.badgeIncrementFlag = false
                    push.badgeSetting = 1
                    push.sound = "default"
                    push.category = "CATEGORY001"
                    // Android用の設定
                    push.action = "com.sample.pushsample.RECEIVE_PUSH"
                    push.title = "家族ダイアリー"
                    push.dialog = true
                    push.sendInBackground { e ->
                        if (e != null) {
                            // エラー処理
                        }
                    }
                }
            }
        }.addOnFailureListener {
            Functions.showAlertOneButton(this, "エラー", it.toString())
        }.addOnCompleteListener {
            // プログレスバー非表示
            binding.overlay.visibility = View.GONE
            binding.progressbar.visibility = View.GONE
        }
    }

    // 送信ボタンの有効/無効の切り替え
    private fun updateSendButtonEnable() {
        if (binding.kibunEditText.text.count() in 1..300 && selectedKibun != null) {
            binding.kibunSendButton.isEnabled = true
            binding.kibunSendButton.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_30dp)
        } else {
            binding.kibunSendButton.isEnabled = false
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

    // 日にち選択ダイアログ表示
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            {_, year, month, dayOfMonth ->
                sendDiaryDateString = "${year}年${String.format("%02d", month + 1)}月${String.format("%02d", dayOfMonth)}日"
                if (Functions.getYearFromDate(Date()) == year && Functions.getMontshFromDate(Date()) == month && Functions.getDayFromDate(Date()) == dayOfMonth) {
                    sendDiaryDate = Date()
                    binding.todayText.text = "今日の日記"
                } else {
                    sendDiaryDate = Functions.getDateFromString("${year}/${month + 1}/${dayOfMonth} 23:59:59")!!
                    binding.todayText.text = "${month + 1}月${dayOfMonth}日の日記"
                }
                sendDiaryTime = Timestamp(sendDiaryDate)
            },
            Functions.getYearFromDate(sendDiaryDate),
            Functions.getMontshFromDate(sendDiaryDate),
            Functions.getDayFromDate(sendDiaryDate)
        )
        datePickerDialog.show()
    }
}