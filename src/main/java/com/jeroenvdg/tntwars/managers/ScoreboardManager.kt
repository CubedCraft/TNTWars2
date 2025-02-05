package com.jeroenvdg.tntwars.managers

import com.jeroenvdg.tntwars.EventBus
import com.jeroenvdg.tntwars.TNTWars
import com.jeroenvdg.tntwars.game.Team
import com.jeroenvdg.tntwars.player.TNTWarsPlayer
import com.jeroenvdg.tntwars.player.PlayerManager
import com.jeroenvdg.minigame_utilities.parseTime

class ScoreboardManager {
    companion object {
        const val RED_LIVES_PARAM = "red_lives"
        const val BLUE_LIVES_PARAM = "blue_lives"
        const val TIME_PARAM = "time"

        const val STATS_WINS_PARAM = "stats_wins"
        const val STATS_KILLS_PARAM = "stats_kills"
        const val STATS_KILLSTREAK_PARAM = "stats_killstreak"
        const val STATS_DEATHS_PARAM = "stats_deaths"
        const val STATS_COINS_PARAM = "stats_coins"
        const val STATS_SCORE_PARAM = "stats_score"

        val instance get() = TNTWars.instance.scoreboardManager
    }

    private var redLives: Int = 8
    private var blueLives: Int = 8
    private var timeLeft: String = "10:00"

    init {
        EventBus.onPlayerJoined += ::handlePlayerJoined
    }

    fun dispose() {
        EventBus.onPlayerJoined -= ::handlePlayerJoined
    }

    fun setTime(seconds: Int) {
        timeLeft = parseTime(seconds)
        setSBParamForEveryone(TIME_PARAM, parseTime(seconds))
    }

    fun setTeamLives(team: Team, i: Int) {
        when(team) {
            Team.Red -> setRedLives(i)
            Team.Blue -> setBlueLives(i)
            else -> {}
        }
    }

    fun setBlueLives(lives: Int) {
        blueLives = lives
        setSBParamForEveryone(BLUE_LIVES_PARAM, lives.toString())
    }

    fun setRedLives(lives: Int) {
        redLives = lives
        setSBParamForEveryone(RED_LIVES_PARAM, lives.toString())
    }

    private fun setSBParamForEveryone(param: String, value: String) {
        for (user in PlayerManager.instance.players) {
            user.scoreBoard.setParam(param, value)
        }
    }

    private fun handlePlayerJoined(tntWarsPlayer: TNTWarsPlayer) {
        tntWarsPlayer.scoreBoard.setParam(RED_LIVES_PARAM, redLives.toString())
        tntWarsPlayer.scoreBoard.setParam(BLUE_LIVES_PARAM, redLives.toString())
        tntWarsPlayer.scoreBoard.setParam(TIME_PARAM, timeLeft)
    }
}