package com.jeroenvdg.tntwars.managers

import com.jeroenvdg.tntwars.EventBus
import com.jeroenvdg.tntwars.RewardConfig
import com.jeroenvdg.tntwars.TNTWars
import com.jeroenvdg.tntwars.game.Team
import com.jeroenvdg.tntwars.misc.PlayerDeathContext
import com.jeroenvdg.tntwars.player.TNTWarsPlayer
import com.jeroenvdg.tntwars.player.PlayerManager
import com.jeroenvdg.tntwars.services.playerStats.IPlayerStatsService
import com.jeroenvdg.tntwars.services.playerStats.RoundData
import com.jeroenvdg.tntwars.services.userIdentifier.UserIdentifier
import com.jeroenvdg.minigame_utilities.Debug
import com.jeroenvdg.minigame_utilities.Textial
import com.jeroenvdg.minigame_utilities.launchCoroutine
import kotlinx.coroutines.Job
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit

class PlayerStatsManager {

    companion object {
        val instance get() = TNTWars.instance.playerStatsManager
        val winRewards get() = TNTWars.instance.config.rewardConfig.winRewards
        val killRewards get() = TNTWars.instance.config.rewardConfig.killRewards
    }

    private val summaryMap = HashMap<UserIdentifier, PlayerRoundSummary>()

    init {
        EventBus.onPlayerDeath += ::handlePlayerDeath
        EventBus.onPlayerJoined += ::handlePlayerJoined
        EventBus.onPlayerTeamChanged += ::handlePlayerTeamChanged
    }

    fun dispose() {
        EventBus.onPlayerDeath -= ::handlePlayerDeath
        EventBus.onPlayerJoined -= ::handlePlayerJoined
        EventBus.onPlayerTeamChanged -= ::handlePlayerTeamChanged
    }

    fun sendRoundSummaries() {
        val mostValuablePlayer = getMostValuablePlayer()
        val mvpComponent = Textial.msg.format("&8${Textial.doubleArrowSymbol} &fMVP: &6${mostValuablePlayer?.bukkitPlayer?.name ?: "No one"}")
        val lineComponent = Textial.msg.format("&7&m+-------&7< &6Round Summary &7>&m-------+")
        for ((userIdentifier, summary) in summaryMap) {
            val component = Component.text()
                .append(lineComponent).appendNewline()
                .append(mvpComponent).appendNewline()
                .append(Textial.msg.prefixComp).appendNewline()
                .append(Textial.msg.format("&8${Textial.doubleArrowSymbol} &fKills: &6${summary.kills}")).appendNewline()
                .append(Textial.msg.format("&8${Textial.doubleArrowSymbol} &fDeaths: &6${summary.deaths}")).appendNewline()
                .append(Textial.msg.format("&8${Textial.doubleArrowSymbol} &fCoins: &6${summary.coins}")).appendNewline()
                .append(Textial.msg.format("&8${Textial.doubleArrowSymbol} &fScore: &6${summary.score}")).appendNewline()
                .append(lineComponent)

            Bukkit.getPlayer(userIdentifier.uuid)?.sendMessage(component)
        }
    }

    fun getMostValuablePlayer(): TNTWarsPlayer? {
        val availableMVPs = summaryMap.filter { PlayerManager.instance.get(it.key) != null }
        var mostValuablePlayer = availableMVPs.keys.firstOrNull() ?: return null
        var mvpWeight = -1
        for ((player, summary) in availableMVPs) {
            val achievedKills = summary.kills
            val currentWeight = achievedKills
            if (currentWeight > mvpWeight) {
                mostValuablePlayer = player
                mvpWeight = currentWeight
            }
        }

        return PlayerManager.instance.get(mostValuablePlayer)!!
    }

    fun saveAllUsers(): Job {
        return launchCoroutine {
            val users = summaryMap.filter { PlayerManager.instance.get(it.key) != null }.map { val user = PlayerManager.instance.get(it.key)!!; Pair(user, user.stats) }
            val saveResult = IPlayerStatsService.current().saveBulk(users)
            if (saveResult.isFailure) {
                Debug.error(Exception("Bulk save failed! THE ROOM IS ON FIRE", saveResult.exceptionOrNull()))
            }
        }
    }

    fun saveOne(user: TNTWarsPlayer): Job {
        return launchCoroutine {
            val saveResult = IPlayerStatsService.current().save(user, user.stats)
            if (saveResult.isFailure) {
                Debug.error(Exception("Save failed! THE ROOM IS ON FIRE", saveResult.exceptionOrNull()))
            }
        }
    }

    fun saveRoundSummary(roundData: RoundData): Job {
        return launchCoroutine {
            val saveResult = IPlayerStatsService.current().saveRoundSummary(roundData, summaryMap.filter { it.value.team.isGameTeam }.toList())
            if (saveResult.isFailure) {
                Debug.error(Exception("Save failed! THE ROOM IS ON FIRE", saveResult.exceptionOrNull()))
            }
        }
    }

    fun resetRoundStatistics() {
        summaryMap.clear()
        for (user in PlayerManager.instance.players) {
            summaryMap[user.identifier] = PlayerRoundSummary(user.team)
        }
    }

    fun addKill(user: TNTWarsPlayer) {
        user.stats.kills++
        user.stats.killSteak++
        summaryMap[user.identifier]!!.kills++
        applyRewards(user, killRewards)
    }

    fun addDeath(user: TNTWarsPlayer) {
        user.stats.deaths++
        user.stats.killSteak = 0
        summaryMap[user.identifier]!!.deaths++
        user.updateScoreboard()
    }

    fun addWin(user: TNTWarsPlayer) {
        user.stats.wins++
        applyRewards(user, winRewards)
    }

    fun addTeamBalance(user: TNTWarsPlayer) {
        user.stats.teamBalances++
    }

    fun removeCoins(user: TNTWarsPlayer, coins: Int) {
        user.stats.coins -= coins
        user.updateScoreboard()
    }

    fun applyRewards(user: TNTWarsPlayer, reward: RewardConfig, refreshScoreboard: Boolean = true) {
        addScore(user, reward.score * BoosterManager.instance.multiplier)
        addCoins(user, reward.coins * BoosterManager.instance.multiplier)
        if (refreshScoreboard) {
            user.run { updateScoreboard() }
        }
    }

    private fun addCoins(user: TNTWarsPlayer, coins: Int) {
        user.stats.coins += coins
        summaryMap[user.identifier]!!.coins += coins
    }

    private fun addScore(user: TNTWarsPlayer, score: Int) {
        user.stats.score += score
        summaryMap[user.identifier]!!.score += score
        user.updateRank()
    }

    private fun addRoundStats(user: TNTWarsPlayer) {
        if (summaryMap[user.identifier] != null) return
        summaryMap[user.identifier] = PlayerRoundSummary(user.team)
    }

    private fun removeRoundStats(user: TNTWarsPlayer) {
        summaryMap.remove(user.identifier)
    }

    private fun handlePlayerDeath(deathContext: PlayerDeathContext) {
        if (deathContext.hasDamager) addKill(deathContext.damager)
        addDeath(deathContext.user)
    }

    private fun handlePlayerJoined(user: TNTWarsPlayer) {
        addRoundStats(user)
    }

    private fun handlePlayerTeamChanged(user: TNTWarsPlayer, oldTeam: Team) {
        if (!user.team.isGameTeam || !oldTeam.isGameTeam) return
        summaryMap[user.identifier]!!.team = user.team
        addTeamBalance(user)
    }
}

class PlayerRoundSummary(var team: Team) {
    var kills = 0
    var deaths = 0
    var coins = 0
    var score = 0
}