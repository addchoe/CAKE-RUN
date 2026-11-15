package com.cakerun.app

import kotlin.math.*

enum class Stage { DECORATE, DELIVERY, RESULT }
enum class Scenario { GENTLE, CITY, ROUGH }
data class Motion(val speedKmh: Double = 0.0, val longitudinal: Double = 0.0, val lateral: Double = 0.0)

/** All motion units are m/s² except speedKmh. Game thresholds are illustrative, not driving assessments. */
class Mission {
    var stage = Stage.DECORATE
    var cream = 0
    var topping = 0
    var decoration = 0
    var scenario = Scenario.CITY
    var elapsed = 0.0
    var distance = 0.0
    var damage = 0.0
    var harshAcceleration = 0
    var harshBrake = 0
    var harshTurn = 0
    var motion = Motion()
    private var accelerationActive = false
    private var brakeActive = false
    private var turnActive = false
    val score get() = (100 - damage).roundToInt().coerceIn(0,100)
    val grade get() = when { score >= 90 -> "완벽 배송"; score >= 65 -> "무사 배송"; else -> "아슬아슬 배송" }
    fun start() {
        stage = Stage.DELIVERY; elapsed = 0.0; distance = 0.0; damage = 0.0
        harshAcceleration = 0; harshBrake = 0; harshTurn = 0
        accelerationActive = false; brakeActive = false; turnActive = false; motion = Motion()
    }
    fun finish() { if(stage == Stage.DELIVERY) { stage = Stage.RESULT; motion = Motion() } }
    fun tick(dt: Double, input: Motion) {
        if(stage != Stage.DELIVERY || !dt.isFinite() || dt <= 0) return
        if(!input.speedKmh.isFinite() || !input.longitudinal.isFinite() || !input.lateral.isFinite()) return
        val step = dt.coerceAtMost(0.1)
        motion = input.copy(speedKmh=input.speedKmh.coerceIn(0.0,200.0), longitudinal=input.longitudinal.coerceIn(-10.0,10.0), lateral=input.lateral.coerceIn(-10.0,10.0))
        elapsed += step; distance += motion.speedKmh / 3.6 * step
        val a = motion.longitudinal > 2.3; val b = motion.longitudinal < -2.8; val t = abs(motion.lateral) > 2.4
        if(a && !accelerationActive) harshAcceleration++
        if(b && !brakeActive) harshBrake++
        if(t && !turnActive) harshTurn++
        accelerationActive = a; brakeActive = b; turnActive = t
        damage = (damage + (max(0.0,abs(motion.longitudinal)-1.8)*1.8 + max(0.0,abs(motion.lateral)-1.8)*1.5)*step).coerceIn(0.0,100.0)
        if(elapsed >= 60.0) finish()
    }
}

class Simulator {
    private var speed = 0.0
    fun reset() { speed = 0.0 }
    fun sample(t: Double, dt: Double, scenario: Scenario, pulse: String?): Motion {
        val phase = t % 12.0
        val factor = when(scenario) { Scenario.GENTLE -> 0.3; Scenario.CITY -> 0.7; Scenario.ROUGH -> 1.3 }
        var a = when { phase < 4 -> 2.6; phase < 8 -> 0.0; else -> -2.5 } * factor
        var lat = if(phase in 5.0..7.0) sin((phase-5.0)*PI/2)*3.0*factor else 0.0
        if(pulse == "brake") a = -5.0
        if(pulse == "accelerate") a = 4.5
        if(pulse == "turn") lat = 4.0
        speed = (speed + a*dt*3.6).coerceIn(0.0,80.0)
        return Motion(speed,a,lat)
    }
}
