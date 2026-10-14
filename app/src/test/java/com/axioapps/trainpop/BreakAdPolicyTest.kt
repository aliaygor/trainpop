package com.axioapps.trainpop
import org.junit.Assert.*
import org.junit.Test
class BreakAdPolicyTest {
    @Test fun noAdsAndFirstTenLevelsNeverShow() { for(l in 1..10) assertFalse(BreakAdPolicy.eligible(l,false,0,0,1));assertFalse(BreakAdPolicy.eligible(13,true,0,0,1)) }
    @Test fun everyThirdWinAfterTenAndCooldownAreRequired() {
        assertFalse(BreakAdPolicy.eligible(11,false,0,0,1));assertTrue(BreakAdPolicy.eligible(13,false,0,0,1))
        assertFalse(BreakAdPolicy.eligible(13,false,13,1,200000));assertFalse(BreakAdPolicy.eligible(16,false,13,1000,120999))
        assertTrue(BreakAdPolicy.eligible(16,false,13,1000,121000))
    }
}
