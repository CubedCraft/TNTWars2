package com.jeroenvdg.tntwars.game.states

import com.jeroenvdg.tntwars.EventBus
import com.jeroenvdg.tntwars.TNTWars
import com.jeroenvdg.tntwars.game.Team
import com.jeroenvdg.tntwars.game.TeamSelectMode
import com.jeroenvdg.tntwars.managers.ScoreboardManager
import com.jeroenvdg.tntwars.player.TNTWarsPlayer
import com.jeroenvdg.tntwars.player.PlayerManager
import com.jeroenvdg.tntwars.player.gameContexts.SpectatorPlayerContext

class WaitingState : BaseGameState() {

    override val playerContextProvider = SpectatorPlayerContext.Provider()
    override val teamSelectMode get() = TeamSelectMode.Queue

    override fun onActivate() {
        EventBus.onPlayerTeamChanged += ::handlePlayerTeamChanged

        PlayerManager.instance.updatePlayerVisibility()

        ScoreboardManager.instance.setTime(TNTWars.instance.config.gameConfig.matchTime)
        ScoreboardManager.instance.setRedLives(8)
        ScoreboardManager.instance.setBlueLives(8)
    }

    override fun onDeactivate() {
        EventBus.onPlayerTeamChanged -= ::handlePlayerTeamChanged
    }

    private fun handlePlayerTeamChanged(user: TNTWarsPlayer, team: Team) {
        if (Team.Queue.usersInTeam().size <= 1) return
        stateMachine.gotoState(CountdownState::class.java)
    }
}