package com.flipperstreaks.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthDetailsResponse(
    @SerialName("user_email") val userEmail: String,
    @SerialName("authentication_token") val authenticationToken: String,
    val username: String? = null
)

@Serializable
data class GenericApiResponse(
    val errors: String? = null
)
