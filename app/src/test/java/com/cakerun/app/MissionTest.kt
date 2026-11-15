package com.cakerun.app
import org.junit.Assert.*
import org.junit.Test
class MissionTest {
 @Test fun gentleDeliveryFinishesWithoutDamage() { val m=Mission(); m.start(); repeat(601){m.tick(.1,Motion(20.0,.5,.4))}; assertEquals(Stage.RESULT,m.stage); assertEquals(100,m.score) }
 @Test fun eventsCountEdgesAndDamageAccumulates() { val m=Mission(); m.start(); repeat(10){m.tick(.1,Motion(30.0,-5.0,4.0))}; assertEquals(1,m.harshBrake); assertEquals(1,m.harshTurn); assertTrue(m.damage>0); m.tick(.1,Motion()); m.tick(.1,Motion(10.0,-5.0)); assertEquals(2,m.harshBrake) }
 @Test fun invalidSamplesAreIgnoredAndDamageClamped() { val m=Mission(); m.start(); m.tick(.1,Motion(Double.NaN)); assertEquals(0.0,m.elapsed,0.0); repeat(600){m.tick(.1,Motion(20.0,10.0,10.0))}; assertEquals(0,m.score) }
 @Test fun resultDoesNotKeepScoringAndRestartResets() { val m=Mission(); m.start(); m.tick(.1,Motion(10.0,-5.0)); m.finish(); val d=m.damage; m.tick(.1,Motion(20.0,10.0)); assertEquals(d,m.damage,0.0); m.start(); assertEquals(100,m.score); assertEquals(0,m.harshBrake) }
 @Test fun roughScenarioIsWorseThanGentle() { fun run(s:Scenario):Int { val m=Mission(); val sim=Simulator(); m.start(); repeat(1200){ m.tick(.05,sim.sample(m.elapsed,.05,s,null)) }; return m.score }; assertTrue(run(Scenario.GENTLE)>run(Scenario.ROUGH)) }
}
