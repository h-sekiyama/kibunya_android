package com.sekky.kibunya.Login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.sekky.kibunya.R
import com.sekky.kibunya.databinding.ActivitySendEmailBinding

class SendEmailActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivitySendEmailBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_send_email)
    }
}