package com.jeroenvdg.tntwars.managers

import com.jeroenvdg.tntwars.TNTWars
import com.jeroenvdg.tntwars.player.TNTWarsPlayer
import com.jeroenvdg.tntwars.services.boosterService.ActiveBooster
import com.jeroenvdg.tntwars.services.boosterService.Booster
import com.jeroenvdg.tntwars.services.boosterService.IBoosterService
import com.google.gson.JsonObject
import com.jeroenvdg.minigame_utilities.*
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import kotlin.math.max

class BoosterManager {

    companion object {
        val instance get() = TNTWars.instance.boosterManager
        val boosterDuration = 60 * 1000 * 60 // 1 hour
        val boosterStrength = 2
    }

    var multiplier = 1; private set;

    init {
        runBlocking {
            val boosterResult = IBoosterService.current().getActiveBoosters()
            if (boosterResult.isFailure) {
                Debug.error(Exception("Could not load boosters", boosterResult.exceptionOrNull()!!))
                return@runBlocking
            }

            for (activeBooster in boosterResult.getOrThrow().reversed()) {
                addActiveBooster(activeBooster)
            }
        }
    }

    fun activateBooster(user: TNTWarsPlayer, booster: Booster) = JobResult {
        if (booster.hasBeenActivated) return@JobResult Result.failure(Exception("Booster has already been activated"))
        booster.hasBeenActivated = true

        val result = IBoosterService.current().activateBooster(user, booster)
        if (result.isFailure) {
            booster.hasBeenActivated = false
            Debug.error(Exception("Could not activate booster ${booster.id} for ${user.bukkitPlayer.name}", result.exceptionOrNull()!!))
            return@JobResult Result.failure<ActiveBooster>(result.exceptionOrNull()!!)
        }

        val activeBooster = result.getOrThrow()
        user.boosters.remove(booster)
        addActiveBooster(activeBooster)

        Bukkit.broadcast(Component.text()
            .append(Textial.bc.format("&7&m+------------------------------------------+").appendNewline()
            .append(Textial.bc.format("&aOne hour Booster has been activated by ").append(user.bukkitPlayer.displayName()).appendNewline()
            .append(Textial.bc.format("&aThe current multiplier is $multiplier").appendNewline())
            .append(Textial.bc.format("&7&m+------------------------------------------+"))))
            .build())

        Soundial.playAll(Soundial.DragonGrowl)

//        if (TNTWars.instance.config.discordConfig.enabled) {
//            Scheduler.async {
//                val p = user.bukkitPlayer
//                val req = JsonObject()
//                req.addProperty("action", "message")
//                req.addProperty("channel_id", "171037025347043329")
//                req.addProperty("title", TNTWars.instance.config.discordConfig.tntwarsChannelId)
//                req.addProperty("message", "A one hour booster has been activated by " + p.name + ", enabling triple Score and Coins if you play now!")
//
//                val req2 = JsonObject()
//                req2.addProperty("action", "message")
//                req2.addProperty("channel_id", TNTWars.instance.config.discordConfig.generalChannelId)
//                req2.addProperty("title", "TNTWars Booster has been activated by " + p.name)
//                req2.addProperty("message", "A one hour booster has been activated by " + p.name + ", enabling triple Score and Coins if you play now!")
//
//                val alert = JsonObject()
//                alert.addProperty("action", "command")
//                alert.addProperty("command", "alert &6&l[&c&lTNTWars&6&l] &aA one hour booster has been activated by &b" + p.name + "&a, enabling triple Score and Coins if you play now on &b/server TNTWars!")
//            }
//        }

        return@JobResult Result.success(activeBooster)
    }

    private fun addActiveBooster(booster: ActiveBooster) {
        val expiresAt = booster.activatedAt + boosterDuration
        val timeLeft = expiresAt - System.currentTimeMillis()
        val timeLeftInTicks = (timeLeft / 1000) * 20

        fun removeBooster() = launchCoroutine {
            val result = IBoosterService.current().removeActiveBooster(booster)
            if (result.isSuccess) {
                Debug.log("Removed expired booster from active boosters list")
            } else {
                Debug.error(Exception("Failed to remove active booster ${booster.id}", result.exceptionOrNull()!!))
            }
        }

        if (timeLeftInTicks < 0) {
            removeBooster()
            return
        }

        multiplier += boosterStrength

        launchCoroutine {
            Scheduler.delay(timeLeftInTicks)
            multiplier = max(1, multiplier - boosterStrength)

            Bukkit.broadcast(Textial.bc.format("&a&lA booster has ended! If you wish to extend it check out /boosters"))
            Bukkit.broadcast(Textial.bc.format("&a&lA booster has ended! If you wish to extend it check out /boosters"))

            removeBooster().await()
        }
    }
}