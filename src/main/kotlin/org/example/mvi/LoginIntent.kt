package org.example.mvi

data class LoginIntent(
    val name: String,
    val ip: String,
    val port: Int
)