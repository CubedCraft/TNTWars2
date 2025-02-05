package com.jeroenvdg.tntwars.commands

import com.jeroenvdg.minigame_utilities.Soundial
import com.jeroenvdg.minigame_utilities.commands.CommandData
import com.jeroenvdg.minigame_utilities.commands.CommandError
import com.jeroenvdg.minigame_utilities.commands.CommandHandler
import com.jeroenvdg.minigame_utilities.commands.builders.SingleCommandBuilder
import com.jeroenvdg.tntwars.player.PlayerManager
import org.bukkit.entity.Player

class TeamChatCommand : CommandHandler() {

    init {
        builder(SingleCommandBuilder("teamchat") {
            stringParam("message", false)
            execute(::teamChatCommand)
        })
    }

    private fun teamChatCommand(data: CommandData, sender: Player) {
        val user = PlayerManager.instance.get(sender) ?: throw CommandError("You must be entangled in the system to use this command")

        if (data.hasParam<String>("message")) {
            val wasInTeamChat = user.teamChatEnabled
            user.teamChatEnabled = true
            sender.chat(data.getParam<String>("message"))
            user.teamChatEnabled = wasInTeamChat
        } else {
            user.teamChatEnabled = !user.teamChatEnabled
            if (user.teamChatEnabled) {
                sender.sendMessage(data.parse("&6Team chat &aEnabled"))
            } else {
                sender.sendMessage(data.parse("&6Team chat &cDisabled"))
            }
            Soundial.play(sender, Soundial.Success)
        }
    }

}