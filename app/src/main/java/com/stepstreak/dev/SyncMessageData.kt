package com.stepstreak.dev

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncMessageData(
    @SerialName("syncX") val syncX: String,

    @SerialName("syncY") val syncY: String

)