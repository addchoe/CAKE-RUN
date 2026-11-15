package com.cakerun.app

/** App-owned boundary. This is not a Pleos SDK API. Motion remains explicitly simulated. */
interface PlatformBridge {
    fun start(onStatus: (String) -> Unit, onArrival: () -> Unit)
    fun requestDemoRoute()
    fun stop()
}
