package com.sekky.kibunya.Login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.Kibunlist.MainActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.Users
import com.sekky.kibunya.databinding.ActivitySignUpBinding
import java.util.concurrent.TimeUnit


class SignUpActivity: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // 既にログイン済みならメイン画面に遷移する
        val isLogin: Boolean = auth.currentUser != null
        if (isLogin) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val binding: ActivitySignUpBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        // 背景タップでキーボードを隠すための処理
        Functions.addBackgroundFocus(binding.background)
        Functions.addKeyboardHide(this, binding.nameInput)
        Functions.addKeyboardHide(this, binding.mailInput)
        Functions.addKeyboardHide(this, binding.passwordInput)
        Functions.addKeyboardHide(this, binding.telInput)

        // 名前入力ボックスの入力監視
        binding.nameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // nop
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateMailRegistorButtonEnable(binding)
            }

            override fun afterTextChanged(s: Editable?) {
                updateMailRegistorButtonEnable(binding)
            }
        })
        // アドレス入力ボックスの入力監視
        binding.mailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // nop
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateMailRegistorButtonEnable(binding)
            }

            override fun afterTextChanged(s: Editable?) {
                updateMailRegistorButtonEnable(binding)
            }
        })
        // パスワード入力ボックスの入力監視
        binding.passwordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // nop
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateMailRegistorButtonEnable(binding)
            }

            override fun afterTextChanged(s: Editable?) {
                updateMailRegistorButtonEnable(binding)
            }
        })
        // 電話番号入力ボックスの入力監視
        binding.telInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // nop
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateTelRegistorButtonEnable(binding)
            }

            override fun afterTextChanged(s: Editable?) {
                updateTelRegistorButtonEnable(binding)
            }
        })

        // メールアドレスで登録処理
        binding.registrationButton.setOnClickListener {

            val nameText = binding.nameInput.text.toString()
            val emailText = binding.mailInput.text.toString()
            val passText = binding.passwordInput.text.toString()

            auth.createUserWithEmailAndPassword(emailText, passText)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        // 認証メール送信
                        user!!.sendEmailVerification()

                        // ユーザー名を更新
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(nameText)
                            .build()
                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // 新規ユーザー登録
                                    val db = FirebaseFirestore.getInstance()
                                    db.collection("users")
                                        .document(user.uid)
                                        .set(hashMapOf("name" to nameText))
                                        .addOnSuccessListener {
                                            val intent = Intent(this, SendEmailActivity::class.java)
                                            startActivity(intent)
                                        }.addOnFailureListener { e ->
                                            Functions.showAlertOneButton(this, "エラー", Functions.getJapaneseErrorMessage(task.exception!!.message.toString()))
                                        }
                                } else {
                                    Functions.showAlertOneButton(this, "エラー", Functions.getJapaneseErrorMessage(task.exception!!.message.toString()))
                                }
                            }
                    } else {
                        Functions.showAlertOneButton(this, "エラー", Functions.getJapaneseErrorMessage(task.exception!!.message.toString()))
                    }
                }
        }

        // メールログイン画面への遷移処理
        binding.toMailLogin.setOnClickListener {
            val intent = Intent(this, EmailLoginActivity::class.java)
            startActivity(intent)
        }

        // 電話番号認証ボタンタップ
        binding.sendAuthentication.setOnClickListener {
            val formattedPhoneNumber: String = "+81" + binding.telInput.text.drop(1)
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(formattedPhoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    // 電話番号認証コールバック
    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        // 即時認証完了
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // 新規ユーザー登録
            val user = auth.currentUser
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this@SignUpActivity) { task ->
                    if (task.isSuccessful) {
                        // 新規ユーザー登録
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users")
                            .document(user!!.uid)
                            .set(Users("名無しのネコちゃん"))
                            .addOnSuccessListener {
                                val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                                startActivity(intent)
                            }.addOnFailureListener { e ->
                                Functions.showAlertOneButton(this@SignUpActivity, "エラー", Functions.getJapaneseErrorMessage(task.exception!!.message.toString()))
                            }
                    } else {
                        Functions.showAlertOneButton(this@SignUpActivity, "エラー", Functions.getJapaneseErrorMessage(task.exception!!.message.toString()))
                    }
            }.addOnFailureListener { e ->
                Functions.showAlertOneButton(this@SignUpActivity, "エラー", e.toString())
            }
        }

        // エラー
        override fun onVerificationFailed(e: FirebaseException) {
            if (e is FirebaseAuthInvalidCredentialsException) {
                Functions.showAlertOneButton(this@SignUpActivity, "エラー", "認証コードの期限が切れています")
            } else if (e is FirebaseTooManyRequestsException) {
                Functions.showAlertOneButton(this@SignUpActivity, "エラー", "リクエスト回数が多すぎます")
            } else {
                Functions.showAlertOneButton(this@SignUpActivity, "エラー", e.toString())
            }
        }

        // 認証コードをSMSで送信完了
        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // 認証IDをPreferenceに保存しておく
            val dataStore: SharedPreferences = getSharedPreferences("DataStore", Context.MODE_PRIVATE)
            val editor = dataStore.edit()
            editor.putString(getString(R.string.family_diary_verificationId), verificationId)
            editor.apply()
            // 認証コード入力画面に遷移
            val intent = Intent(this@SignUpActivity, SmsSendCompleteActivity::class.java)
            startActivity(intent)
        }
    }

    // アドレスで登録ボタンの有効/無効の切り替え
    fun updateMailRegistorButtonEnable(binding: ActivitySignUpBinding) {
        if (binding.nameInput.text.count() in 1..16 && binding.mailInput.text.count() > 0 && binding.passwordInput.text.count() > 0) {
            binding.registrationButton.isClickable = true
            binding.registrationButton.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_30dp)
        } else {
            binding.registrationButton.isClickable = false
            binding.registrationButton.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)
        }
    }
    // 電話番号認証ボタンの有効/無効の切り替え
    fun updateTelRegistorButtonEnable(binding: ActivitySignUpBinding) {
        if (binding.telInput.text.count() == 11) {
            binding.sendAuthentication.isClickable = true
            binding.sendAuthentication.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_30dp)
        } else {
            binding.sendAuthentication.isClickable = false
            binding.sendAuthentication.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)
        }
    }
}