package com.jeroenvdg.tntwars.commands

import com.jeroenvdg.minigame_utilities.commands.CommandData
import com.jeroenvdg.minigame_utilities.commands.CommandHandler
import com.jeroenvdg.minigame_utilities.commands.builders.SingleCommandBuilder
import com.jeroenvdg.tntwars.interfaces.SettingsInterface
import org.bukkit.entity.Player

class SettingsCommand : CommandHandler() {

    init {
        builder(SingleCommandBuilder("settings") {
            execute(::openSettings)
        })
    }

    private fun openSettings(data: CommandData, player: Player) {
        SettingsInterface.open(player)
    }

}
