package com.sekky.kibunya.Other

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.nifcloud.mbaas.core.NCMBInstallation
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.Families
import com.sekky.kibunya.KibunInput.KibunInputActivity
import com.sekky.kibunya.Kibunlist.MainActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityAddFamilyBinding
import kotlinx.android.synthetic.main.tab_layout.view.*
import org.json.JSONArray

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
            // プログレスバー表示
            binding.overlay.visibility = View.VISIBLE
            binding.progressbar.visibility = View.VISIBLE

            searchUser(user!!)
        }

        // 家族追加ボタンタップ
        binding.addFamilyButton.setOnClickListener {
            // プログレスバー表示
            binding.overlay.visibility = View.VISIBLE
            binding.progressbar.visibility = View.VISIBLE

            val db: FirebaseFirestore = FirebaseFirestore.getInstance()

            val familyRef = db.collection("families")
            val containsMyUidDocument = familyRef.whereArrayContains("user_id", user!!.uid)   // 自分のIDを含むドキュメントを検索
            val containsAddUserUidDocument = familyRef.whereArrayContains("user_id", binding.userIdInput.text.toString())  // 追加しようとしているユーザーのIDを含むドキュメントを検索

            containsMyUidDocument.get().addOnSuccessListener { result1 ->
                containsAddUserUidDocument.get().addOnSuccessListener { result2 ->
                    if (result1.count() == 0 && result2.count() == 0) { // 自分も相手もまだ家族がいない時
                        db.collection("families").document().set(
                            Families(listOf(binding.userIdInput.text.toString(), user.uid))
                        ).addOnSuccessListener {
                            // 家族登録状況を元にニフクラにfamilyIdを登録
                            val installation = NCMBInstallation.getCurrentInstallation()
                            installation.channels = JSONArray("[${db.collection("families").document().id}]")
                            installation.saveInBackground()

                            updateActionAfterAddFamily()
                        }.addOnFailureListener {
                            Functions.showAlertOneButton(
                                this@AddFamilyActivity,
                                "エラー",
                                it.message.toString()
                            )
                        }.addOnCompleteListener {
                            // プログレスバー非表示
                            binding.overlay.visibility = View.GONE
                            binding.progressbar.visibility = View.GONE
                        }
                    } else if (result1.count() != 0 && result2.count() != 0) {   // どっちも家族がいる時
                        if (result1.documents[0].id == result2.documents[0].id) {    // 既に家族になってる時
                            binding.addFamilyCompleteLabel.visibility = View.VISIBLE
                            binding.addFamilyCompleteText.text = "は既に家族登録済みです"
                        } else if (result1.documents[0].id != result2.documents[0].id) {  // どちらにも別々の家族がいる時
                            // 家族をマージ
                            val familyList = result2.documents[0].get("user_id") as List<*>
                            for (i in 0..familyList.size - 1) {
                                db.collection("families")
                                    .document(result1.documents[0].id)
                                    .update("user_id", FieldValue.arrayUnion(familyList[i]))
                            }
                            // 2つ目の家族は削除
                            db.collection("families")
                                .document(result2.documents[0].id)
                                .delete()
                                .addOnSuccessListener {
                                    updateActionAfterAddFamily()
                                }
                        }
                        // プログレスバー非表示
                        binding.overlay.visibility = View.GONE
                        binding.progressbar.visibility = View.GONE
                    } else if (result1.count() != 0 || result2.count() != 0) {
                        if (result1.count() != 0 && result2.count() == 0) {  // 自分にだけ家族がいる時
                            db.collection("families")
                                .document(result1.documents[0].id)
                                .update("user_id", FieldValue.arrayUnion(binding.userIdInput.text.toString()))
                                .addOnSuccessListener {
                                    updateActionAfterAddFamily()
                                }.addOnFailureListener {
                                    Functions.showAlertOneButton(
                                        this@AddFamilyActivity,
                                        "エラー",
                                        it.message.toString()
                                    )
                                }.addOnCompleteListener {
                                    // プログレスバー非表示
                                    binding.overlay.visibility = View.GONE
                                    binding.progressbar.visibility = View.GONE
                                }
                        } else if (result1.count() == 0 && result2.count() != 0) {  // 相手にだけ家族がいる時
                            db.collection("families")
                                .document(result2.documents[0].id)
                                .update("user_id", FieldValue.arrayUnion(user.uid))
                                .addOnSuccessListener {
                                    // 家族登録状況を元にニフクラにfamilyIdを登録
                                    val installation = NCMBInstallation.getCurrentInstallation()
                                    installation.channels = JSONArray("[${db.collection("families").document().id}]")
                                    installation.saveInBackground()

                                    updateActionAfterAddFamily()
                                }.addOnFailureListener {
                                    Functions.showAlertOneButton(
                                        this@AddFamilyActivity,
                                        "エラー",
                                        it.message.toString()
                                    )
                                }.addOnCompleteListener {
                                    // プログレスバー非表示
                                    binding.overlay.visibility = View.GONE
                                    binding.progressbar.visibility = View.GONE
                                }
                        }
                    }
                }
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
                            "http://kazoku-diary?kazokuDiary=" + binding.myUserIdInput.text.toString() + "\n\n" +
                            "アプリをダウンロード\n" +
                            "iOS版：https://apps.apple.com/us/app/id1528947553\n\n" +
                            "Android版：https://play.google.com/store/apps/details?id=com.sekky.kibunya"
                )
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        // タブボタン処理
        binding.tabLayout.tab_button2.setImageResource(R.drawable.tab2_on)
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

    // ユーザーを検索する処理
    private fun searchUser(user: FirebaseUser) {
        // 入力したのが自分のユーザーIDの場合
        if (binding.userIdInput.text.toString() == user.uid ?: "") {
            binding.searchedUserName.text = getString(R.string.my_own_user_id_label)
            binding.searchedUserName.visibility = View.VISIBLE

            // プログレスバー非表示
            binding.overlay.visibility = View.GONE
            binding.progressbar.visibility = View.GONE
        } else {
            val docRef = db.collection("users").document(binding.userIdInput.text.toString())
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document.data != null) {
                        // HITしたユーザー名を表示
                        binding.searchedUserName.text = document.get("name").toString()
                        binding.searchedUserName.visibility = View.VISIBLE
                        binding.addFamilyButton.isEnabled = true
                        binding.addFamilyButton.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_30dp)
                    } else {
                        binding.searchedUserName.text = "該当するユーザーがいません"
                        binding.searchedUserName.visibility = View.VISIBLE
                    }
                }.addOnCompleteListener {
                    // プログレスバー非表示
                    binding.overlay.visibility = View.GONE
                    binding.progressbar.visibility = View.GONE
                }
        }
    }

    // ユーザーを追加したあとの諸々の処理
    private fun updateActionAfterAddFamily() {
        // 追加したユーザー名を表示
        binding.newFamilyName.text = binding.searchedUserName.text
        binding.addFamilyCompleteLabel.visibility = View.VISIBLE
        binding.addFamilyCompleteText.text = getString(R.string.add_family_complete_text)
        // 家族追加ボタンをdisabledにする
        binding.addFamilyButton.isEnabled = false
        binding.addFamilyButton.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)
        updateSearchButtonEnable()
    }

    // プロフィール変更ボタンの有効/無効の切り替え
    private fun updateSearchButtonEnable() {
        if (binding.userIdInput.text.count() == 28) {
            binding.searchButton.isEnabled = true
            binding.searchButton.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_30dp)
        } else {
            binding.searchButton.isEnabled = false
            binding.searchButton.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)
        }
    }
}