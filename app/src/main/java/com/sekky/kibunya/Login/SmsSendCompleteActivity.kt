package com.sekky.kibunya.Login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.Kibunlist.MainActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.Users
import com.sekky.kibunya.databinding.ActivitySmsSendCompleteBinding

class SmsSendCompleteActivity: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val binding: ActivitySmsSendCompleteBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_sms_send_complete)

        // 背景タップでキーボードを隠すための処理
        Functions.addBackgroundFocus(binding.background, this)

        // 認証コード入力ボックスの入力監視
        binding.credentialCodeInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // nop
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateRegistorOrLoginButtonEnable(binding)
            }

            override fun afterTextChanged(s: Editable?) {
                updateRegistorOrLoginButtonEnable(binding)
            }
        })

        // 登録 or ログイン処理
        binding.registrationOrLoginButton.setOnClickListener {
            // プログレスバー非表示
            binding.overlay.visibility = View.VISIBLE
            binding.progressbar.visibility = View.VISIBLE

            // 認証IDをPreferenceから取得
            val dataStore: SharedPreferences = getSharedPreferences("DataStore", Context.MODE_PRIVATE)
            val verificationID: String? = dataStore.getString(getString(R.string.family_diary_verificationId), "")

            val credential = PhoneAuthProvider.getCredential(verificationID!!, binding.credentialCodeInput.text.toString())

            signInWithPhoneAuthCredential(credential, binding)
        }

        // 新規登録画面へ遷移
        binding.toRegistration.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, binding: ActivitySmsSendCompleteBinding) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    val name: String?
                    name = if (binding.nameInput.text.toString() == "" && user!!.displayName == null) {
                        "名無しの猫ちゃん"
                    } else if (user!!.displayName != null) {
                        user.displayName
                    } else {
                        binding.nameInput.text.toString()
                    }

                    // 新規ユーザー登録
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users")
                        .document(user.uid)
                        .set(Users(name))
                        .addOnSuccessListener {
                            // プログレスバー非表示
                            binding.overlay.visibility = View.GONE
                            binding.progressbar.visibility = View.GONE

                            if (user.displayName != "") {
                                val userProfileChangeRequest =
                                    UserProfileChangeRequest.Builder().setDisplayName(name).build()
                                user.updateProfile(userProfileChangeRequest)
                                    .addOnCompleteListener {
                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                    }
                            }
                        }.addOnFailureListener { e ->
                            // プログレスバー非表示
                            binding.overlay.visibility = View.GONE
                            binding.progressbar.visibility = View.GONE

                            Functions.showAlertOneButton(this, "エラー", Functions.getJapaneseErrorMessage(task.exception!!.message.toString()))
                        }
                } else {
                    // プログレスバー非表示
                    binding.overlay.visibility = View.GONE
                    binding.progressbar.visibility = View.GONE

                    Functions.showAlertOneButton(this, "エラー", Functions.getJapaneseErrorMessage(task.exception!!.message.toString()))
                }
            }
    }

    // 登録 or ログインボタンの有効/無効の切り替え
    private fun updateRegistorOrLoginButtonEnable(binding: ActivitySmsSendCompleteBinding) {
        if (binding.credentialCodeInput.text.count() == 6) {
            binding.registrationOrLoginButton.isEnabled = true
            binding.registrationOrLoginButton.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_30dp)
        } else {
            binding.registrationOrLoginButton.isEnabled = false
            binding.registrationOrLoginButton.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)
        }
    }
}