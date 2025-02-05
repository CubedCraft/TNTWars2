package com.jeroenvdg.tntwars.commands

import com.jeroenvdg.minigame_utilities.commands.CommandHandler
import com.jeroenvdg.minigame_utilities.commands.builders.SingleCommandBuilder
import com.jeroenvdg.tntwars.interfaces.ProfileInterface

class ProfileCommand : CommandHandler() {
    init {
        builder(SingleCommandBuilder("profile") {
            execute { data, sender ->
                ProfileInterface.open(sender)
            }
        })
    }
}