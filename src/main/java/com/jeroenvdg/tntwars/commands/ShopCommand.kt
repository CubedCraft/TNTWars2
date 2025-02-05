package com.jeroenvdg.tntwars.commands

import com.jeroenvdg.minigame_utilities.commands.CommandData
import com.jeroenvdg.minigame_utilities.commands.CommandError
import com.jeroenvdg.minigame_utilities.commands.CommandHandler
import com.jeroenvdg.minigame_utilities.commands.builders.SingleCommandBuilder
import com.jeroenvdg.tntwars.interfaces.ShopInterface
import com.jeroenvdg.tntwars.player.PlayerManager
import org.bukkit.entity.Player

class ShopCommand : CommandHandler(){

    init {
        builder(SingleCommandBuilder("Shop") {
            execute(::openShop)
        })
    }

    fun openShop(data: CommandData, player: Player) {
        val user = PlayerManager.instance.get(player) ?: throw CommandError("You are not in the game")
        if (user.team.isSpectatorTeam) throw CommandError("Spectators cannot open the shop")
        ShopInterface.open(player)
    }

}