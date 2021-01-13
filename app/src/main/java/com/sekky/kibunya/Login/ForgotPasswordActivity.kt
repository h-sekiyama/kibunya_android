package com.sekky.kibunya.Login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityForgotPasswordBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_forgot_password)
    }
}