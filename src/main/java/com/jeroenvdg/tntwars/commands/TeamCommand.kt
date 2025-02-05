package com.jeroenvdg.tntwars.commands

import com.jeroenvdg.minigame_utilities.commands.CommandError
import com.jeroenvdg.minigame_utilities.commands.CommandHandler
import com.jeroenvdg.minigame_utilities.commands.builders.SingleCommandBuilder
import com.jeroenvdg.tntwars.game.GameManager
import com.jeroenvdg.tntwars.interfaces.TeamSelector

class TeamCommand : CommandHandler() {
    init {
        builder(SingleCommandBuilder("team") {
            execute { data, player ->
                if (!GameManager.instance.teamSelectMode.isJoinable) {
                    throw CommandError("You cannot open the teamselector right now")
                }
                TeamSelector.open(player)
            }
        })
    }
}