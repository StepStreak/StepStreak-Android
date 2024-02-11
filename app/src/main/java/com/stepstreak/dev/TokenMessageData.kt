package com.stepstreak.dev

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenMessageData(
    @SerialName("syncToken") val token: String
)