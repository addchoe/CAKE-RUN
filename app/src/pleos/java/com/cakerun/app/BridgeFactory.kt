package com.cakerun.app

import android.content.Context
import android.os.Handler
import android.os.Looper
import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.model.capability.AccessType
import ai.pleos.playground.vehicle.listener.BrakePedalStatusListener
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener
import ai.pleos.playground.navi.constants.NaviErrorCode
import ai.pleos.playground.navi.data.DestinationArrivedInfo
import ai.pleos.playground.navi.data.DestinationInfo
import ai.pleos.playground.navi.data.RouteInfo

fun createBridge(context: Context): PlatformBridge = PleosBridge(context.applicationContext)

/** Official lifecycle + brake read + Navi route adapter. Never invents physical motion from brake Boolean. */
private class PleosBridge(private val context: Context): PlatformBridge {
    private val main = Handler(Looper.getMainLooper())
    private var current: Session? = null
    private inner class Session(val report: (String)->Unit, val arrived: ()->Unit) {
        val vehicle = Vehicle(context)
        val navi = NaviHelper(context)
        val brake by lazy { vehicle.getBrake() }
        var open = true
        var pending = 0
        var releasedVehicle = false
        var brakeRegistered = false
        var naviRegistered = false
        var naviConnected = false
        var routeRequested = false
        var vehicleStatus = "Vehicle 연결 확인 중"
        var naviStatus = "Navi 연결 확인 중"
        fun display() { if(open && current===this) report("MOTION SIMULATION · $vehicleStatus · $naviStatus") }
        fun releaseVehicleIfIdle() {
            if(!open && pending==0 && !releasedVehicle) { releasedVehicle=true; runCatching { vehicle.release() } }
        }
        val brakeListener = object: BrakePedalStatusListener {
            override fun onBrakePedalStatusUpdated(status: Boolean?) { main.post {
                vehicleStatus = "브레이크 ${when(status) { true->"ON"; false->"OFF"; null->"UNKNOWN" }} (실제 SDK)"; display()
            } }
            override fun onFailed(exception: Exception) { main.post { vehicleStatus="Vehicle 읽기 실패: ${exception.javaClass.simpleName}"; display() } }
        }
        val listener = object: NaviHelperEventListener {
            override fun onRoutingCompleted() { main.post { naviStatus="경로 탐색 완료"; display(); if(open) runCatching { navi.getDestinationInfo() } } }
            override fun onDestinationInfo(destinationInfo: DestinationInfo) { main.post {
                naviStatus="${destinationInfo.name} · 잔여 ${destinationInfo.remainDistance}m / ${destinationInfo.remainTime}초"; display()
            } }
            override fun onDestinationArrived(destinationArrivedInfo: DestinationArrivedInfo) { main.post {
                if(open && current===this@Session && routeRequested) { routeRequested=false; naviStatus="SDK 목적지 도착"; display(); arrived() }
            } }
            override fun onError(errorCode: NaviErrorCode) { main.post { routeRequested=false; naviStatus="Navi 오류: $errorCode"; display() } }
        }
    }
    override fun start(onStatus: (String)->Unit, onArrival: ()->Unit) {
        stop()
        val s=Session(onStatus,onArrival); current=s; s.display()
        runCatching {
            s.vehicle.initialize()
            s.pending++
            s.brake.checkBrakePedalStatusCapabilities(onComplete={ config -> main.post {
                s.pending--
                if(s.open) {
                    if(config.accessType==AccessType.READ || config.accessType==AccessType.READ_WRITE) {
                        runCatching { s.brake.registerBrakePedalStatus(s.brakeListener); s.brakeRegistered=true }
                            .onFailure { s.vehicleStatus="Vehicle 권한/구독 실패" }
                        s.pending++
                        runCatching { s.brake.getCurrentBrakePedalStatus(onComplete={ value -> main.post {
                            s.pending--; s.brakeListener.onBrakePedalStatusUpdated(value); s.releaseVehicleIfIdle()
                        } },onFailed={ error -> main.post {
                            s.pending--; s.brakeListener.onFailed(error); s.releaseVehicleIfIdle()
                        } }) }.onFailure { s.pending--; s.vehicleStatus="Vehicle 조회 실패" }
                    } else s.vehicleStatus="브레이크 읽기 미지원"
                    s.display()
                }
                s.releaseVehicleIfIdle()
            } },onFailed={ error -> main.post {
                s.pending--; s.vehicleStatus="Vehicle 사용 불가: ${error.javaClass.simpleName}"; s.display(); s.releaseVehicleIfIdle()
            } })
        }.onFailure { s.pending=0; s.vehicleStatus="Vehicle 연결 실패"; s.display() }
        runCatching { s.navi.initialize(onConnected={ main.post {
            if(s.open && current===s) {
                runCatching { s.navi.addListener(s.listener); s.naviRegistered=true; s.naviConnected=true; s.naviStatus="Navi 연결됨" }
                    .onFailure { s.naviStatus="Navi 리스너 등록 실패" }
                s.display()
            }
        } },onFailed={ error -> main.post { s.naviStatus="Navi 사용 불가: ${error.javaClass.simpleName}"; s.display() } }) }
            .onFailure { s.naviStatus="Navi 초기화 실패"; s.display() }
        main.postDelayed({ if(s.open && current===s) {
            if(s.vehicleStatus=="Vehicle 연결 확인 중") s.vehicleStatus="Vehicle 응답 대기 · 가상 주행 유지"
            if(!s.naviConnected) s.naviStatus="Navi 응답 대기 · 가상 배송 유지"
            s.display()
        } },5000)
    }
    override fun requestDemoRoute() {
        val s=current ?: return
        if(!s.naviConnected) { s.naviStatus="연결 후 다시 요청하세요"; s.display(); return }
        runCatching {
            s.routeRequested=true
            // Exact named fields from the official Kotlin destinations example. This is a demo POI.
            s.navi.requestRoute(RouteInfo(latitude=37.48544722, longitude=127.03636666,
                displayLatitude=null, displayLongitude=null, poiName="42dot", poiId="36khp37hh8gqcm5nqhe5",
                poiSubId="0", address="서울시 강남구 남부순환로 2621"))
            s.naviStatus="공식 예제 목적지 탐색 요청 중"; s.display()
        }.onFailure { s.routeRequested=false; s.naviStatus="경로 요청 실패"; s.display() }
    }
    override fun stop() {
        val s=current ?: return; current=null; s.open=false
        if(s.brakeRegistered) runCatching { s.brake.unregisterBrakePedalStatus(s.brakeListener) }
        if(s.naviRegistered) runCatching { s.navi.removeListener(s.listener) }
        runCatching { s.navi.release() }
        // Vehicle get/capability calls are asynchronous: release after outstanding callbacks settle.
        s.releaseVehicleIfIdle()
    }
}
