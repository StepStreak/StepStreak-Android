package com.stepstreak.dev.strada

import dev.hotwire.strada.BridgeComponentFactory

val bridgeComponentFactories = listOf(
    BridgeComponentFactory("sync", ::SyncButtonComponent),
    BridgeComponentFactory("token", ::TokenComponent)
)
