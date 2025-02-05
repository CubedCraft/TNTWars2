package com.jeroenvdg.tntwars

import com.jeroenvdg.tntwars.player.PlayerManager
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class PlaceholderAPI(val plugin: Plugin) : PlaceholderExpansion() {

    private val placeholders = HashMap<String, (Player) -> String?>()

    init {
        val playerManager = PlayerManager.instance
        placeholders["wins"] = { playerManager.get(it)?.stats?.wins?.toString() ?: "0" }
        placeholders["kills"] = { playerManager.get(it)?.stats?.kills?.toString() ?: "0" }
        placeholders["deaths"] = { playerManager.get(it)?.stats?.deaths?.toString() ?: "0" }
        placeholders["killstreak"] = { playerManager.get(it)?.stats?.killSteak?.toString() ?: "0" }
        placeholders["coins"] = { playerManager.get(it)?.stats?.coins?.toString() ?: "0" }
        placeholders["exp"] = { playerManager.get(it)?.stats?.score?.toString() ?: "0" }
    }

    override fun getIdentifier(): String {
        return "tntwars"
    }

    override fun getAuthor(): String {
        return "Jeroeno_Boy"
    }

    override fun getVersion(): String {
        return plugin.pluginMeta.version
    }

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        if (player == null) return null
        val fn = placeholders[params.lowercase()]
        return if (fn == null) null else fn(player)
    }
}