package org.example.connections

import java.io.Serializable

data class User(
    val uuid: String,
    var login: String = "name $uuid"
) : Serializable