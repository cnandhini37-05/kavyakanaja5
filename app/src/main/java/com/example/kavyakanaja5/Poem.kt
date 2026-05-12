package com.example.kavyakanaja5

data class Poem(
    val id: String,
    val title: String,
    val author: String,
    val kannadaLines: List<String>,
    val englishLines: List<String>,
    val bhavartha: String,
    val audioResId: Int? = null // For the speech.mp3
)