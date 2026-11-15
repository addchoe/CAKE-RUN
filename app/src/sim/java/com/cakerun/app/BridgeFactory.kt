package com.cakerun.app
import android.content.Context
fun createBridge(context: Context): PlatformBridge = object: PlatformBridge {
    override fun start(onStatus: (String)->Unit, onArrival: ()->Unit) { onStatus("SIMULATION · 차량 데이터 미사용") }
    override fun requestDemoRoute() {}
    override fun stop() {}
}
