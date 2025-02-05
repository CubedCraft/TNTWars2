package com.jeroenvdg.tntwars.player

import com.jeroenvdg.minigame_utilities.*
import com.jeroenvdg.minigame_utilities.scoreboard.DisplayBoard
import com.jeroenvdg.tntwars.EventBus
import com.jeroenvdg.tntwars.TNTWars
import com.jeroenvdg.tntwars.game.Team
import com.jeroenvdg.tntwars.managers.ScoreboardManager
import com.jeroenvdg.tntwars.managers.achievements.AchievementsManager
import com.jeroenvdg.tntwars.misc.PlayerDeathContext
import com.jeroenvdg.tntwars.services.achievements.CompletedAchievement
import com.jeroenvdg.tntwars.services.boosterService.Booster
import com.jeroenvdg.tntwars.services.boosterService.IBoosterService
import com.jeroenvdg.tntwars.services.playerSettings.IPlayerSettingsService
import com.jeroenvdg.tntwars.services.playerSettings.PlayerSettings
import com.jeroenvdg.tntwars.services.playerStats.IPlayerStatsService
import com.jeroenvdg.tntwars.services.playerStats.PlayerStats
import com.jeroenvdg.tntwars.services.userIdentifier.IUserIdentifierService
import com.jeroenvdg.tntwars.services.userIdentifier.UserIdentifier
import com.jeroenvdg.tntwars.services.vanishService.IPlayerVanishService
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent

class TNTWarsPlayer(player: Player) {

    val bukkitPlayer = player
    val behaviourCollection = PlayerBehaviourCollection(this)

    lateinit var identifier: UserIdentifier; private set
    lateinit var settings: PlayerSettings; private set
    lateinit var stats: PlayerStats; private set
    lateinit var achievements: Array<CompletedAchievement?>; private set
    lateinit var boosters: MutableList<Booster>; private set

    val onTeamChanged = Event2<Team, Team>()
    val onDamaged = Event1<EntityDamageEvent>()
    val onPlayerDeath = Event1<PlayerDeathContext>()
    val onBlockPlaced = Event1<BlockPlaceEvent>()
    val onPlayerMoved = Event1<PlayerMoveEvent>()
    val onInteract = Event1<PlayerInteractEvent>()
    val onHandItemSwap = Event1<PlayerSwapHandItemsEvent>()
    val onInventoryReset = Event0()

    val isVanishMode get() = IPlayerVanishService.current().isPlayerVanish(this)
    val canSeeVanish get() = bukkitPlayer.hasPermission("cubedcraft.staff")

    var isGodMode = false
    var teamChatEnabled = false
    var ignoreTeamBounds = false

    val scoreBoard = DisplayBoard(player)

    private val stateMachine = PlayerStateMachine(this) // Trust me, exposing this will do more harm than good, if one ever wants to expose this, contact Jeroen!!
    private var cachedDisplayRank: String? = null

    var team: Team = Team.Spectator
        set(value) {
            if (field == value) return
            val previous = field
            field = value
            updateUserTab()
            onTeamChanged.invoke(previous, value)
            EventBus.onPlayerTeamChanged.invoke(this, previous)
        }

    fun init(): Job {
        scoreBoard
            .setTitle("&6&lTNTWars &f- [${ScoreboardManager.TIME_PARAM}]")
            .registerSection("lives", "&6Lives", 10) { it
                .addLine("&cRed: [${ScoreboardManager.RED_LIVES_PARAM}]")
                .addLine("&9Blue: [${ScoreboardManager.BLUE_LIVES_PARAM}]")
                .addLine("")
            }
            .registerSection("stats", "&6Stats", 5) {it
                .addLine("&fWins: &6[${ScoreboardManager.STATS_WINS_PARAM}]")
                .addLine("&fKills: &6[${ScoreboardManager.STATS_KILLS_PARAM}]")
                .addLine("&fKillstreak: &6[${ScoreboardManager.STATS_KILLSTREAK_PARAM}]")
                .addLine("&fCoins: &6[${ScoreboardManager.STATS_COINS_PARAM}]")
                .addLine("&fScore: &6[${ScoreboardManager.STATS_SCORE_PARAM}]")
                .addLine("")
            }
            .registerSection("ip", "&6cubedcraft.com", -99) {}

        return launchCoroutine { scope ->
            val identifierResult = IUserIdentifierService.current().getIdentifier(this)
            identifier = identifierResult.getOrElse {
                bukkitPlayer.kick(Textial.msg.parse("Unable to load your user identifier, please contact staff if this is a reoccurring issue"))
                throw it
            }

            var statsResult: Result<PlayerStats>? = null
            var settingsResult: Result<PlayerSettings>? = null
            var achievementsResult: Result<Array<CompletedAchievement?>>? = null
            var boosterResult: Result<List<Booster>>? = null

            statsResult = IPlayerStatsService.current().load(this@TNTWarsPlayer)

            val jobs = arrayOf(
                scope.launch { settingsResult = IPlayerSettingsService.current().loadSettings(this@TNTWarsPlayer) },
                scope.launch { achievementsResult = AchievementsManager.instance.loadPlayerAchievements(this@TNTWarsPlayer) },
                scope.launch { boosterResult = IBoosterService.current().getBoostersForPlayer(this@TNTWarsPlayer) }
            )

            for (job in jobs) { job.await() }

            stats = statsResult.getOrElse {
                bukkitPlayer.kick(Textial.msg.parse("Unable to load your stats, please contact staff if this is a reoccurring issue"))
                throw it
            }

            settings = settingsResult!!.getOrElse {
                bukkitPlayer.kick(Textial.msg.parse("Unable to load your settings, please contact staff if this is a reoccurring issue"))
                throw it
            }

            achievements = achievementsResult!!.getOrElse {
                bukkitPlayer.kick(Textial.msg.parse("Unable to load your achievements, please contact staff if this is a reoccuring issue"))
                throw it
            }

            boosters = boosterResult!!.getOrElse {
                bukkitPlayer.kick(Textial.msg.parse("Unable to load your boosters, please contact staff if this is a reoccuring issue"))
                throw it
            }.toMutableList()

            updateScoreboard()
            updateUserTab()
            stateMachine.activate()
        }
    }

    fun dispose(): Job {
        stateMachine.deactivate()
        for (behaviour in behaviourCollection.behaviours) {
            behaviour.value.deactivate()
        }

        return launchCoroutine { scope ->
            val jobs = arrayOf(
                scope.launch { IPlayerSettingsService.current().saveSettings(this@TNTWarsPlayer, settings) },
                scope.launch { IPlayerStatsService.current().save(this@TNTWarsPlayer, stats) },
            )

            for (job in jobs) { job.await() }
        }
    }

    fun updateUserTab() {
        bukkitPlayer.playerListName(Component.text("${getRank()} ${bukkitPlayer.name}").color(team.primaryColor.color))
    }

    fun updateScoreboard() {
        scoreBoard.setParam(ScoreboardManager.STATS_WINS_PARAM, stats.wins.toString())
        scoreBoard.setParam(ScoreboardManager.STATS_KILLS_PARAM, stats.kills.toString())
        scoreBoard.setParam(ScoreboardManager.STATS_KILLSTREAK_PARAM, stats.killSteak.toString())
        scoreBoard.setParam(ScoreboardManager.STATS_DEATHS_PARAM, stats.deaths.toString())
        scoreBoard.setParam(ScoreboardManager.STATS_COINS_PARAM, stats.coins.toString())
        scoreBoard.setParam(ScoreboardManager.STATS_SCORE_PARAM, stats.score.toString())
    }

    fun heal() {
        bukkitPlayer.health = bukkitPlayer.getAttribute(Attribute.MAX_HEALTH)?.defaultValue ?: 20.0
        bukkitPlayer.foodLevel = 20
    }

    fun resetInventory() {
        bukkitPlayer.inventory.clear()
        onInventoryReset.invoke()
    }

    fun getRank(): String {
        if (cachedDisplayRank == null) updateRank()
        return cachedDisplayRank!!
    }

    fun updateRank() {
        val availableRanks = TNTWars.instance.ranksConfig.ranks
        val playerScore = stats.score
        var currentRank = availableRanks.first()
        for (rank in availableRanks) {
            if (rank.score > playerScore) break
            currentRank = rank
        }

        val previousDisplayRank = cachedDisplayRank
        cachedDisplayRank = currentRank.rank

        if (previousDisplayRank != cachedDisplayRank) {
            updateUserTab()
        }
    }

    private fun handleDamaged(event: EntityDamageEvent) {
        if (!isGodMode) return
        event.isCancelled = true
        event.damage = 0.0
    }
}