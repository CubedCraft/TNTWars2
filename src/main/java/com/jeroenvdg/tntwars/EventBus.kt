package com.jeroenvdg.tntwars

import com.jeroenvdg.minigame_utilities.Event0
import com.jeroenvdg.minigame_utilities.Event1
import com.jeroenvdg.minigame_utilities.Event2
import com.jeroenvdg.minigame_utilities.commands.builders.params.CommandParameter
import com.jeroenvdg.minigame_utilities.commands.builders.params.IntCommandParameter
import com.jeroenvdg.tntwars.commands.parameters.TeamParameter
import com.jeroenvdg.tntwars.game.MatchEndReason
import com.jeroenvdg.tntwars.game.Team
import com.jeroenvdg.tntwars.game.TeamSelectMode
import com.jeroenvdg.tntwars.managers.mapManager.ActiveMap
import com.jeroenvdg.tntwars.misc.PlayerDeathContext
import com.jeroenvdg.tntwars.player.TNTWarsPlayer
import com.jeroenvdg.tntwars.player.gameContexts.IPlayerGameContext
import org.bukkit.event.Cancellable

enum class InfluenceType(val params: Collection<CommandParameter>) {
    setLives(listOf(TeamParameter("team", true, includeSpectatorTeam = false), IntCommandParameter("lives", true, min = 1))),
    setTimer(listOf(IntCommandParameter("minutes", true, min = 1)))
}

class EventBus {
    companion object {

        val onPlayerJoined = Event1<TNTWarsPlayer>()
        val onPlayerLeft = Event1<TNTWarsPlayer>()
        val onPlayerTeamChanged = Event2<TNTWarsPlayer, Team>()
        val onPlayerDeath = Event1<PlayerDeathContext>()
        val onTeamSelectorModeChanged = Event1<TeamSelectMode>()
        val onTNTSpawnEvent = Event1<TNTSpawnEvent>()
        val onMapChanged = Event1<ActiveMap>()

        val onMatchStarted = Event0()
        val onPlayerGameContextProviderChanged = Event1<IPlayerGameContext.IProvider>()
        val onMatchEnded = Event1<MatchEndReason>()
        val onFlawlessMatchEnded = Event1<Team>()
        val onUserVanishChanged = Event1<TNTWarsPlayer>()

        val onAdminGameInfluence = Event2<InfluenceType, List<Any>>()

        fun reset() {
            onPlayerJoined.clear()
            onPlayerLeft.clear()
            onPlayerTeamChanged.clear()
            onPlayerDeath.clear()
            onTeamSelectorModeChanged.clear()
            onUserVanishChanged.clear()
            onMapChanged.clear()

            onMatchStarted.clear()
            onMatchEnded.clear()
            onPlayerGameContextProviderChanged.clear()

            onAdminGameInfluence.clear()
        }
    }
}

data class TNTSpawnEvent(val team: Team?, val ownerId: String?) : Cancellable {

    private var cancelled = false

    override fun isCancelled() = cancelled
    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }
}