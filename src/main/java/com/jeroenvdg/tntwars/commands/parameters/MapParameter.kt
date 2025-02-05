package com.jeroenvdg.tntwars.commands.parameters

import com.jeroenvdg.minigame_utilities.commands.CommandData
import com.jeroenvdg.minigame_utilities.commands.builders.SingleCommandBuilder
import com.jeroenvdg.minigame_utilities.commands.builders.params.CommandParameter
import com.jeroenvdg.tntwars.TNTWars
import com.jeroenvdg.tntwars.managers.mapManager.MapManager
import com.jeroenvdg.tntwars.managers.mapManager.TNTWarsMap
import org.bukkit.entity.Player

class MapParameter(name: String, required: Boolean, private val mustBeEnabled: Boolean) : CommandParameter(name, required) {
    private val mapManager: MapManager = TNTWars.instance.mapManager

    override fun execute(data: CommandData, sender: Player) {
        val s = (nextWord(data) ?: return).lowercase()
        val map = mapList().firstOrNull { it.id == s }
        if (map == null) {
            throw error("&p$s &rIs not a valid map")
        }

        data.setParam(name, map)
    }

    override fun tabComplete(data: CommandData, sender: Player): List<String>? {
        val s = data.consumer.consumeWord().lowercase()
        if (data.consumer.hasNext()) return null
        return mapList().map { it.id }.filter { it.startsWith(s) }
    }

    private fun mapList(): Collection<TNTWarsMap> {
        return if (mustBeEnabled) {
            mapManager.enabledElements
        } else {
            mapManager
        }
    }
}

fun SingleCommandBuilder.mapParam(name: String, required: Boolean, mustBeEnabled: Boolean = false) {
    add(MapParameter(name, required, mustBeEnabled))
}

