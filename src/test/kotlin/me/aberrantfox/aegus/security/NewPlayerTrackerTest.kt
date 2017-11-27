package me.aberrantfox.aegus.security

import me.aberrantfox.aegeus.listeners.antispam.NewPlayers
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class NewPlayerTrackerTest {

    @Before
    fun beforeEach() = NewPlayers.cache.clear()

    @Test
    fun aPlayerJoiningShouldBeTracked() {
        NewPlayers.cache.put("a", DateTime.now())
        NewPlayers.cache.put("b", DateTime.now())
        NewPlayers.cache.put("c", DateTime.now())
        NewPlayers.cache.put("d", DateTime.now())

        Assert.assertEquals(4, NewPlayers.pastMins(1).size)
    }

    @Test
    fun playersJoiningOutOfScopeShouldntBeListed() {
        NewPlayers.cache.put("a", DateTime.now())
        Thread.sleep(100)
        Assert.assertEquals(0, NewPlayers.pastMins(0).size)
    }
}