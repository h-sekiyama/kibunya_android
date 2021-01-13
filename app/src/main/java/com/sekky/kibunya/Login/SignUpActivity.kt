package com.sekky.kibunya.Login

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivitySignUpBinding


class SignUpActivity: AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivitySignUpBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        Functions.addBackgroundFocus(binding.background)
        Functions.addKeyboardHide(this, binding.nameInput)
        Functions.addKeyboardHide(this, binding.mailInput)
        Functions.addKeyboardHide(this, binding.passwordInput)
        Functions.addKeyboardHide(this, binding.telInput)

        // 名前入力ボックスの入力監視
        binding.nameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
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
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateTelRegistorButtonEnable(binding)
            }

            override fun afterTextChanged(s: Editable?) {
                updateTelRegistorButtonEnable(binding)
            }
        })
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