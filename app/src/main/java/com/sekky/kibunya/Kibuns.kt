package com.sekky.kibunya

data class Kibuns(
    val date: String? = "",
    val documentId: String? = "",
    val image: String? = "",
    val kibun: Int? = 0,
    val name: String? = "",
    val text: String? = "",
    val time: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val user_id: String? = ""
)