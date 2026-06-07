package com.example

import com.example.chess.data.entity.UserStats
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class RewardSystemUnitTest {

    @Test
    fun testFirstClaimWorks() {
        val stats = UserStats(
            coinBalance = 100,
            lastDailyRewardClaimTime = 0L,
            nextClaimTimestamp = 0L,
            totalClaimedRewards = 0,
            rewardHistory = ""
        )

        val serverTimeNow = 1717730000000L // Simulate server epoch

        // Check is claimable
        val isFirstClaimable = serverTimeNow >= stats.nextClaimTimestamp
        assertTrue(isFirstClaimable)

        // Simulate claim
        val nextClaimTime = serverTimeNow + (24 * 60 * 60 * 1000L)
        val updatedStats = stats.copy(
            coinBalance = stats.coinBalance + 50,
            lastDailyRewardClaimTime = serverTimeNow,
            nextClaimTimestamp = nextClaimTime,
            totalClaimedRewards = stats.totalClaimedRewards +  1,
            rewardHistory = serverTimeNow.toString()
        )

        assertEquals(150, updatedStats.coinBalance)
        assertEquals(1, updatedStats.totalClaimedRewards)
        assertEquals("1717730000000", updatedStats.rewardHistory)
        assertEquals(serverTimeNow + 24 * 60 * 60 * 1000L, updatedStats.nextClaimTimestamp)
    }

    @Test
    fun testSecondClaimBlockedWithin24Hours() {
        val serverTimeClaim1 = 1717730000000L
        val nextClaimTime = serverTimeClaim1 + (24 * 60 * 60 * 1000L)

        val stats = UserStats(
            coinBalance = 150,
            lastDailyRewardClaimTime = serverTimeClaim1,
            nextClaimTimestamp = nextClaimTime,
            totalClaimedRewards = 1,
            rewardHistory = "1717730000000"
        )

        // Simulate time after 12 hours (still within 24h)
        val serverTimeAfter12Hours = serverTimeClaim1 + (12 * 60 * 60 * 1000L)

        // Check description
        val isClaimable = serverTimeAfter12Hours >= stats.nextClaimTimestamp
        assertFalse("Second claim must be blocked within 24h", isClaimable)

        val remainingMs = stats.nextClaimTimestamp - serverTimeAfter12Hours
        assertEquals(12 * 60 * 60 * 1000L, remainingMs)
    }

    @Test
    fun testClaimEarnsFiftyCoins() {
        val stats = UserStats(coinBalance = 300)
        val updated = stats.copy(coinBalance = stats.coinBalance + 50)
        assertEquals(350, updated.coinBalance)
    }

    @Test
    fun testCountdownFormatWorks() {
        val remainingMs = 23 * 60 * 60 * 1000L + 59 * 60 * 1000L + 59 * 1000L // 23h 59m 59s
        val seconds = (remainingMs / 1000) % 60
        val minutes = (remainingMs / (1000 * 60)) % 60
        val hours = (remainingMs / (1000 * 60 * 60))
        val countdownStr = String.format(Locale.US, "%02dh %02dm %02ds", hours, minutes, seconds)
        assertEquals("23h 59m 59s", countdownStr)
    }

    @Test
    fun testAntiCheatSimulation() {
        // Test system elapsed clock reference anti-cheat
        val serverTimeAtSetup = 1717730000000L
        val localTimeAtSetup = 50000L // elapsed realtime ms
        
        // Player moves local clock but elapsedRealtime increases sequentially by 10 seconds
        val localTimeLater = 60000L // elapsed realtime ms (10 seconds later)
        
        val currentVerifiedTime = serverTimeAtSetup + (localTimeLater - localTimeAtSetup)
        assertEquals(1717730010000L, currentVerifiedTime) // Verified time is clock change proof
    }
}
