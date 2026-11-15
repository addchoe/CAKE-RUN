package com.cakerun.app

import android.content.Context
import android.graphics.*
import android.view.View
import kotlin.math.*

/** Procedural 2.5D cake. No remote assets or 3D runtime required. */
class CakeView @JvmOverloads constructor(context: Context, private val mission: Mission = Mission()): View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val crack = Path().apply { moveTo(58f,10f); lineTo(42f,35f); lineTo(60f,51f); lineTo(39f,77f) }
    private val creamColors = intArrayOf(Color.parseColor("#FFE3CB"), Color.parseColor("#F4A6BD"), Color.parseColor("#806055"))
    private var tilt = 0f
    private var squash = 1f
    fun animateFrame() {
        val target = (-mission.motion.lateral * 4 + mission.motion.longitudinal * 1.6).toFloat().coerceIn(-18f,18f)
        tilt += (target-tilt)*0.09f
        squash += ((1f-abs(mission.motion.longitudinal.toFloat())*0.012f)-squash)*0.1f
        invalidate()
    }
    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val s = min(width/500f,height/380f)
        c.save(); c.translate(width/2f,height/2f+30*s); c.scale(s,s)
        p.color = Color.parseColor("#263538"); c.drawOval(-205f,108f,205f,146f,p)
        p.color = Color.parseColor("#80908B"); c.drawOval(-180f,92f,180f,127f,p)
        c.save(); c.rotate(tilt,0f,100f); c.scale(1f,squash,0f,100f)
        val cream = creamColors[mission.cream]
        p.color = Color.parseColor("#CA9D76"); c.drawRoundRect(-142f,-26f,142f,98f,16f,16f,p)
        p.color = Color.parseColor("#F8E5BF"); c.drawRect(-141f,20f,141f,46f,p)
        p.color = Color.parseColor("#D99286"); c.drawRect(-141f,53f,141f,65f,p)
        p.color = cream; c.drawOval(-142f,69f,142f,112f,p); c.drawRect(-142f,-28f,142f,85f,p)
        p.color = Color.parseColor("#FFF0DA"); c.drawRect(-141f,22f,141f,38f,p)
        p.color = cream; c.drawOval(-142f,-73f,142f,15f,p)
        p.color = Color.argb(35,255,255,255); c.drawOval(-128f,-65f,120f,-2f,p)
        p.color = cream
        for(i in 0..8) c.drawRoundRect(-140f+i*32,-26f,-120f+i*32,if(i%2==0) 11f else 2f,12f,12f,p)
        val count = if(mission.damage>45) 3 else 7
        for(i in 0 until count) {
            val angle = i*2*PI/7
            val x = (cos(angle)*100).toFloat(); val y = (sin(angle)*27-29).toFloat()
            p.color = Color.parseColor(if(mission.topping==0) "#EE675B" else if(mission.topping==1) "#715575" else "#F5C75E")
            c.drawOval(x-12,y-18,x+12,y+7,p)
            p.color = Color.parseColor("#819C73"); c.drawOval(x-7,y-20,x+5,y-13,p)
        }
        if(mission.decoration==1) {
            p.color=Color.parseColor("#75C7C3"); c.drawRoundRect(-4f,-117f,4f,-49f,3f,3f,p)
            p.color=Color.parseColor("#FFD178"); c.drawOval(-7f,-136f,7f,-117f,p)
        } else {
            p.color=Color.parseColor("#FFF7DD"); c.drawRoundRect(-42f,-110f,42f,-57f,8f,8f,p)
            p.color=Color.parseColor("#AE6052"); p.textSize=18f; p.textAlign=Paint.Align.CENTER; c.drawText("CAKE",0f,-77f,p)
        }
        if(mission.damage>20) {
            p.color=Color.parseColor("#9B7663"); p.strokeWidth=4f; p.style=Paint.Style.STROKE
            c.drawPath(crack,p); p.style=Paint.Style.FILL
        }
        c.restore()
        if(mission.damage>45) { p.color=cream; c.drawOval(120f,106f,175f,123f,p) }
        c.restore()
    }
}
