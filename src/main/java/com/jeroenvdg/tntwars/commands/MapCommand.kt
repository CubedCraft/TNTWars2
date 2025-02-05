package com.jeroenvdg.tntwars.commands

import com.jeroenvdg.minigame_utilities.commands.CommandHandler
import com.jeroenvdg.minigame_utilities.commands.builders.SingleCommandBuilder
import com.jeroenvdg.tntwars.interfaces.MapSelector

class MapCommand : CommandHandler() {
    init {
        builder(SingleCommandBuilder("map") {
            execute { data, player ->
                MapSelector.open(player)
            }
        })
    }
}