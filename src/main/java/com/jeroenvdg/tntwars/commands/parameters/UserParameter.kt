package com.jeroenvdg.tntwars.commands.parameters

import com.jeroenvdg.minigame_utilities.commands.CommandData
import com.jeroenvdg.minigame_utilities.commands.builders.SingleCommandBuilder
import com.jeroenvdg.minigame_utilities.commands.builders.params.CommandParameter
import com.jeroenvdg.tntwars.player.PlayerManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class UserParameter(name: String, required: Boolean, private val allowAll: Boolean) : CommandParameter(name, required) {
    private val playerManager = PlayerManager.instance

    override fun execute(data: CommandData, sender: Player) {
        val s = (nextWord(data) ?: return).lowercase()

        if (allowAll) {
            val users = when (s) {
                "@a" -> playerManager.players
                "@spec" -> playerManager.players.filter { it.team.isSpectatorTeam }
                "@game" -> playerManager.players.filter { it.team.isGameTeam }
                else -> null
            }
            if (users != null) {
                data.setParam(name, playerManager.players)
                return
            }
        }

        val player = Bukkit.getPlayer(s) ?: throw error("User does not exist")
        val user = playerManager[player.uniqueId] ?: throw error("User does not exist")
        data.setParam(name, user)
    }

    override fun tabComplete(data: CommandData, sender: Player): List<String>? {
        val s = data.consumer.consumeWord().lowercase()
        if (data.consumer.hasNext()) return null
        val users = playerManager.players.map { it.bukkitPlayer.name }.toMutableList()
        users.addAll(listOf("@a", "@spec", "@game"))
        return users.filter { it.lowercase().startsWith(s) }
    }
}

fun SingleCommandBuilder.userParam(name: String, required: Boolean, allowAll: Boolean = false) {
    add(UserParameter(name, required, allowAll))
}

