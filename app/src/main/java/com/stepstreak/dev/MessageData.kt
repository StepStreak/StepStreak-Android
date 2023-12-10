package com.stepstreak.dev

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageData(
    @SerialName("syncToken") val token: String
)