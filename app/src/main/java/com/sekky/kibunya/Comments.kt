package com.sekky.kibunya

data class Comments(
    val diary_id: String? = "",
    val name: String? = "",
    val text: String? = "",
    val time: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val user_id: String? = ""
)