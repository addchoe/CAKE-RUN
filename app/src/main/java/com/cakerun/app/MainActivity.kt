package com.cakerun.app

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import org.json.JSONObject

class MainActivity: Activity() {
    private val mission = Mission()
    private val sim = Simulator()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var bridge: PlatformBridge
    private lateinit var root: LinearLayout
    private lateinit var cake: CakeView
    private lateinit var metrics: TextView
    private lateinit var status: TextView
    private var sourceStatus = "SIMULATION"
    private var pulse: String? = null
    private var pulseUntil = 0L
    private var last = 0L
    private var active = false
    private val white = Color.parseColor("#F6F2E7")
    private val muted = Color.parseColor("#A5B5AE")
    private val coral = Color.parseColor("#FA876E")
    private val frame = object: Runnable {
        override fun run() {
            if(!active) return
            val now=SystemClock.elapsedRealtime(); val dt=((now-last)/1000.0).coerceIn(0.0,0.05); last=now
            if(now>pulseUntil) pulse=null
            val was=mission.stage
            if(was==Stage.DELIVERY) mission.tick(dt,sim.sample(mission.elapsed,dt,mission.scenario,pulse))
            if(was!=mission.stage) { render(); persist() }
            cake.animateFrame(); updateMetrics()
            handler.postDelayed(this,16)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        restore(); bridge=createBridge(this); render()
    }
    override fun onStart() {
        super.onStart(); active=true; last=SystemClock.elapsedRealtime(); handler.post(frame)
        bridge.start({ message -> runOnUiThread { sourceStatus=message; status.text=sourceStatus } }, {
            runOnUiThread { mission.finish(); render(); persist() }
        })
    }
    override fun onStop() { active=false; handler.removeCallbacks(frame); bridge.stop(); persist(); super.onStop() }
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun bg(color:String, radius:Float=20f)=GradientDrawable().apply { setColor(Color.parseColor(color)); cornerRadius=dp(radius.toInt()).toFloat() }
    private fun label(text:String,size:Float=18f,color:Int=white)=TextView(this).apply { this.text=text; textSize=size; setTextColor(color); setPadding(dp(4),dp(4),dp(4),dp(4)) }
    private fun button(text:String, primary:Boolean=false, action:()->Unit)=Button(this).apply {
        this.text=text; textSize=16f; isAllCaps=false; minHeight=dp(52); setTextColor(if(primary) Color.parseColor("#182B29") else white)
        background=bg(if(primary) "#FA876E" else "#344842",14f)
        setPadding(dp(16),dp(6),dp(16),dp(6)); setOnClickListener { action() }
    }
    private fun addButton(parent:LinearLayout,text:String,primary:Boolean=false,action:()->Unit) {
        parent.addView(button(text,primary,action),LinearLayout.LayoutParams(-1,dp(54)).apply { topMargin=dp(9) })
    }
    private fun render() {
        root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(28),dp(14),dp(28),dp(14)); setBackgroundColor(Color.parseColor("#172C28")) }
        setContentView(root)
        val header=LinearLayout(this).apply { gravity=Gravity.CENTER_VERTICAL }
        header.addView(label("중요한 건 흔들리지 않는 마음",20f).apply { typeface=Typeface.DEFAULT_BOLD },LinearLayout.LayoutParams(0,-2,1f))
        header.addView(label(when(mission.stage) { Stage.DECORATE -> "01  PREPARE"; Stage.DELIVERY -> "02  ON THE ROAD"; Stage.RESULT -> "03  DELIVERED" },14f,muted))
        root.addView(header)
        status=label(sourceStatus,12f,coral); root.addView(status)
        val body=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        root.addView(body,LinearLayout.LayoutParams(-1,0,1f))
        val left=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; background=bg("#223C35"); setPadding(dp(16),dp(12),dp(16),dp(12)) }
        body.addView(left,LinearLayout.LayoutParams(0,-1,1.2f).apply { rightMargin=dp(20) })
        left.addView(label(when(mission.stage) { Stage.DECORATE->"오늘의 배송을 구워볼까요?"; Stage.DELIVERY->"부드러운 여정, 달콤한 도착"; Stage.RESULT->mission.grade },23f).apply { typeface=Typeface.DEFAULT_BOLD })
        cake=CakeView(this,mission).apply { contentDescription="주행에 반응하는 케이크" }
        left.addView(cake,LinearLayout.LayoutParams(-1,0,1f))
        metrics=label("",15f,muted); left.addView(metrics)
        val scroll=ScrollView(this)
        body.addView(scroll,LinearLayout.LayoutParams(0,-1,1f))
        val panel=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }; scroll.addView(panel)
        when(mission.stage) {
            Stage.DECORATE -> {
                panel.addView(label("나만의 케이크",24f))
                panel.addView(label("꾸미기는 출발 전에 완료하세요.",14f,muted))
                addButton(panel,"크림 · "+arrayOf("바닐라","딸기","초콜릿")[mission.cream]) { mission.cream=(mission.cream+1)%3; render(); persist() }
                addButton(panel,"토핑 · "+arrayOf("딸기","블루베리","망고")[mission.topping]) { mission.topping=(mission.topping+1)%3; render(); persist() }
                addButton(panel,"장식 · "+arrayOf("메시지 픽","캔들")[mission.decoration]) { mission.decoration=1-mission.decoration; render(); persist() }
                addButton(panel,"주행 · "+scenarioName()) { mission.scenario=Scenario.entries[(mission.scenario.ordinal+1)%3]; render(); persist() }
                addButton(panel,"60초 시뮬레이션 출발 →",true) { sim.reset(); pulse=null; mission.start(); render(); persist() }
                if(packageName=="com.cakerun.app") addButton(panel,"공식 예제 목적지 경로 요청") { bridge.requestDemoRoute() }
            }
            Stage.DELIVERY -> {
                panel.addView(label("배송 중",24f))
                panel.addView(label("목적지 · 달콤한 작업실\n60초 가상 배송 · "+scenarioName(),16f,muted))
                panel.addView(label("조작 없이 케이크의 변화를 관찰하세요.\n아래 버튼은 데모용 가상 입력입니다.",14f,muted))
                addButton(panel,"가상 급가속") { inject("accelerate") }
                addButton(panel,"가상 급제동") { inject("brake") }
                addButton(panel,"가상 급회전") { inject("turn") }
                addButton(panel,"데모 조기 도착 · 결과 보기",true) { mission.finish(); render(); persist() }
            }
            Stage.RESULT -> {
                panel.addView(label("${mission.score} / 100",42f,coral).apply { typeface=Typeface.DEFAULT_BOLD })
                panel.addView(label("케이크 보존 점수 · 게임용 지표",14f,muted))
                panel.addView(label("급가속  ${mission.harshAcceleration}회\n급제동  ${mission.harshBrake}회\n급회전  ${mission.harshTurn}회",20f))
                panel.addView(label(if(mission.score>=90) "크림과 토핑이 잘 도착했어요." else "다음에는 가속과 회전을 더 부드럽게!",16f,muted))
                addButton(panel,"새 케이크 배송하기",true) { mission.stage=Stage.DECORATE; mission.damage=0.0; render(); persist() }
            }
        }
        root.addView(label("MVP  /  주행·거리·점수는 시뮬레이션입니다. 실제 운전 평가가 아닙니다.",11f,muted))
        updateMetrics()
    }
    private fun scenarioName()=arrayOf("부드럽게","도심","거친 주행")[mission.scenario.ordinal]
    private fun inject(kind:String) { pulse=kind; pulseUntil=SystemClock.elapsedRealtime()+1200 }
    private fun updateMetrics() {
        metrics.text=when(mission.stage) {
            Stage.DECORATE -> "18가지 조합  /  2.5D 케이크 미리보기"
            Stage.DELIVERY -> "가상 속도 %.0f km/h   ·   남은 시간 %.0f초\n케이크 %d%%   ·   가상 거리 %.0fm".format(mission.motion.speedKmh,(60-mission.elapsed).coerceAtLeast(0.0),mission.score,mission.distance)
            Stage.RESULT -> "배송 %.1f초  ·  %.0fm  ·  SIMULATION".format(mission.elapsed,mission.distance)
        }
    }
    private fun persist() {
        val j=JSONObject().put("stage",mission.stage.name).put("cream",mission.cream).put("topping",mission.topping).put("decoration",mission.decoration).put("scenario",mission.scenario.name).put("elapsed",mission.elapsed).put("distance",mission.distance).put("damage",mission.damage).put("a",mission.harshAcceleration).put("b",mission.harshBrake).put("t",mission.harshTurn)
        getSharedPreferences("mission",0).edit().putString("state",j.toString()).apply()
    }
    private fun restore() { runCatching {
        val j=JSONObject(getSharedPreferences("mission",0).getString("state","{}")!!)
        mission.stage=Stage.valueOf(j.optString("stage","DECORATE")); mission.cream=j.optInt("cream").coerceIn(0,2); mission.topping=j.optInt("topping").coerceIn(0,2); mission.decoration=j.optInt("decoration").coerceIn(0,1)
        mission.scenario=Scenario.valueOf(j.optString("scenario","CITY")); mission.elapsed=j.optDouble("elapsed",0.0); mission.distance=j.optDouble("distance",0.0); mission.damage=j.optDouble("damage",0.0)
        mission.harshAcceleration=j.optInt("a"); mission.harshBrake=j.optInt("b"); mission.harshTurn=j.optInt("t")
        // Process death ends a demo cleanly; never fabricate motion during an offline gap.
        if(mission.stage==Stage.DELIVERY) mission.finish()
    } }
}
