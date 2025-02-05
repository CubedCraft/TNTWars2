package com.jeroenvdg.tntwars.commands.parameters

import com.jeroenvdg.minigame_utilities.commands.CommandData
import com.jeroenvdg.minigame_utilities.commands.builders.SingleCommandBuilder
import com.jeroenvdg.minigame_utilities.commands.builders.params.CommandParameter
import com.jeroenvdg.tntwars.game.Team
import org.bukkit.entity.Player

class TeamParameter(name: String, required: Boolean, private val includeSpectatorTeam: Boolean = true) : CommandParameter(name, required) {

    private val teamsMap = HashMap<String, Team>()

    init {
        teamsMap["red"] = Team.Red
        teamsMap["blue"] = Team.Blue
        teamsMap["spectator"] = Team.Spectator
    }

    override fun execute(data: CommandData, sender: Player) {
        val s = (nextWord(data) ?: return).lowercase()
        val team = teamsMap[s] ?: throw error("&p$s &rIs not a valid team")

        if (!includeSpectatorTeam && team == Team.Spectator) {
            throw error("&p$s &rIs not allowed")
        }

        data.setParam("Team", team)
    }

    override fun tabComplete(data: CommandData, sender: Player): List<String>? {
        val s = data.consumer.consumeWord().lowercase()
        if (data.consumer.hasNext()) return null

        return if (includeSpectatorTeam) {
            teamsMap.keys
        } else {
            teamsMap.filter { !it.value.isSpectatorTeam }.keys
        }.filter { it.lowercase().startsWith(s) }
    }
}

fun SingleCommandBuilder.teamParam(name: String, required: Boolean, includeSpectatorTeam: Boolean = true) {
    add(TeamParameter(name, required, includeSpectatorTeam))
}