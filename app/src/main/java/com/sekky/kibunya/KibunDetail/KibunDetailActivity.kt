package com.sekky.kibunya.KibunDetail

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout.VERTICAL
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nifcloud.mbaas.core.NCMBInstallation
import com.nifcloud.mbaas.core.NCMBPush
import com.nifcloud.mbaas.core.NCMBQuery
import com.sekky.kibunya.Comments
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.KibunInput.KibunInputActivity
import com.sekky.kibunya.Kibunlist.MainActivity
import com.sekky.kibunya.Other.OtherActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityKibunDetailBinding
import kotlinx.android.synthetic.main.tab_layout.view.*

class KibunDetailActivity: AppCompatActivity() {

    private val binding: ActivityKibunDetailBinding by lazy {
        DataBindingUtil.setContentView<ActivityKibunDetailBinding>(this, R.layout.activity_kibun_detail)
    }

    private var text: String? = ""
    private var date: String? = ""
    private var name: String? = ""
    private var kibun: Int = 0
    private var time: String? = ""
    private var image: String? = ""
    private var userId: String? = ""
    private var documentId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        text = intent.getStringExtra("text")
        date = intent.getStringExtra("date")
        name = intent.getStringExtra("name")
        kibun = intent.getIntExtra("kibun", 0)
        time = intent.getStringExtra("time")
        image = intent.getStringExtra("image")
        userId = intent.getStringExtra("userId")
        documentId = intent.getStringExtra("documentId")

        // プログレスバー表示
        binding.overlay.visibility = View.VISIBLE
        binding.progressbar.visibility = View.VISIBLE

        // 背景タップでキーボードを隠すための処理
        Functions.addBackgroundFocus(binding.background, this)

        // コメント入力を監視
        binding.commentInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSendButtonEnable()
            }

            override fun afterTextChanged(s: Editable?) {
                updateSendButtonEnable()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        configure()
        showComments()
        binding.commentSendButton.setOnClickListener {
            sendComment()
        }
    }

    fun configure() {

        // CloudStorageを使う準備
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("profileIcon/${userId}.jpg")

        // 日時
        binding.date = date
        // ユーザーアイコン
        imageRef.getBytes(1000000).addOnSuccessListener {
            // 画像が存在すればそれを表示
            Glide.with(this)
                .load(imageRef)
                .placeholder(R.drawable.noimage)
                .signature(ObjectKey(System.currentTimeMillis()))
                .into(binding.userImage)
        }.addOnFailureListener {
            // 取得失敗したらデフォルト画像表示
            binding.userImage.setImageResource(R.drawable.noimage)
        }
        // ユーザー名
        binding.name = name
        // 気分アイコン
        when (kibun) {
            0 -> binding.kibun.setImageResource(R.drawable.kibun_icon0)
            1 -> binding.kibun.setImageResource(R.drawable.kibun_icon1)
            2 -> binding.kibun.setImageResource(R.drawable.kibun_icon2)
            3 -> binding.kibun.setImageResource(R.drawable.kibun_icon3)
            4 -> binding.kibun.setImageResource(R.drawable.kibun_icon4)
        }
        // 時間
        binding.time = time
        // 画像
        if (image != "") {
            Glide.with(this)
                .load(Uri.parse(image))
                .signature(ObjectKey(System.currentTimeMillis()))
                .into(binding.kibunImage)
            binding.kibunImage.visibility = View.VISIBLE
        }

        // 本文
        binding.kibunText = text

        // タブ処理
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

    // コメント表示
    @SuppressLint("WrongConstant")
    fun showComments() {
        binding.commentList.adapter = CommentAdapter()
        binding.commentList.layoutManager = LinearLayoutManager(this, VERTICAL, false)

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val commentRef = db.collection("comments")
        val diaryDocument = commentRef.whereEqualTo("diary_id", documentId!!)
        diaryDocument.get().addOnSuccessListener { resultsMap ->
            val results = resultsMap?.toObjects(Comments::class.java)
                ?.sortedByDescending { comments -> comments.time } ?: return@addOnSuccessListener
            val adapter = binding.commentList.adapter as CommentAdapter
            adapter.addItems(results)
        }.addOnFailureListener {
            Functions.showAlertOneButton(this, "エラー", it.toString())
        }.addOnCompleteListener {
            // プログレスバー非表示
            binding.overlay.visibility = View.GONE
            binding.progressbar.visibility = View.GONE
        }
    }

    // コメント送信
    fun sendComment() {
        // プログレスバー表示
        binding.overlay.visibility = View.VISIBLE
        binding.progressbar.visibility = View.VISIBLE

        val documentId: String? = intent.getStringExtra("documentId")
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val familyRef = db.collection("families")
        val containsMyUidDocument = familyRef.whereArrayContains("user_id", user!!.uid)
        var familyId = ""
        // PUSH通知送信の為にfamilyIdを取得しておく
        containsMyUidDocument.get().addOnSuccessListener { resultMap ->
            if (resultMap.documents.size != 0) {    // 家族がいる場合
                familyId = resultMap.documents[0].id
            }
        }
        db.collection("comments").document().set(
            Comments(
                documentId,
                user.displayName,
                binding.commentInput.text.toString(),
                Timestamp.now(),
                user.uid
            )
        ).addOnSuccessListener {
            binding.commentInput.setText("")
            updateSendButtonEnable()
            showComments()

            if (familyId != "") {   // 家族がいる場合PUSH通知を送信する
                val push = NCMBPush()
                // i/A共通の設定
                push.message = "${user.displayName}が${name}の日記にコメントしました"

                // 送る対象を家族に限定
                val installation = NCMBInstallation.getCurrentInstallation()
                val query = NCMBQuery<NCMBInstallation>("installation")
                query.whereContainedIn("channels", listOf(familyId))
                query.whereNotEqualTo("deviceToken", installation.deviceToken) // 自分は除く
                push.setSearchCondition(query)

                // iOS用の設定
                push.badgeIncrementFlag = true
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
        }.addOnCompleteListener {
            // プログレスバー非表示
            binding.overlay.visibility = View.GONE
            binding.progressbar.visibility = View.GONE
        }
    }

    // 送信ボタンの有効/無効の切り替え
    private fun updateSendButtonEnable() {
        if (binding.commentInput.text.count() > 0) {
            binding.commentSendButton.isEnabled = true
            binding.commentSendButton.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_6dp)
        } else {
            binding.commentSendButton.isEnabled = false
            binding.commentSendButton.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_6dp)
        }
    }
}