package com.sekky.kibunya.Login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivityEmailLoginBinding

class EmailLoginActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityEmailLoginBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_email_login)
    }
}