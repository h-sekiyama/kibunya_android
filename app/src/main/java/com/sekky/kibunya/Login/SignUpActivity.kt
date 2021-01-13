package com.sekky.kibunya.Login

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
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
    }


}