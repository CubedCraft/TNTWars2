package com.jeroenvdg.tntwars.interfaces

import com.jeroenvdg.minigame_utilities.*
import com.jeroenvdg.minigame_utilities.gui.guibuilders.HopperMenu
import com.jeroenvdg.minigame_utilities.gui.guibuilders.IMenu
import com.jeroenvdg.minigame_utilities.gui.player
import com.jeroenvdg.minigame_utilities.gui.slots.addButton
import com.jeroenvdg.tntwars.game.GameManager
import com.jeroenvdg.tntwars.listeners.GenericItemListener
import com.jeroenvdg.tntwars.managers.mapManager.MapManager
import com.jeroenvdg.tntwars.managers.mapManager.TNTWarsMap
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.collections.set

class MapSelector : IPlayerGUI {

    companion object : GUISingleton<MapSelector>("MapSelector") {
        lateinit var mapSelectorItem : ItemStack; private set
    }

    override val name get() = guiName

    lateinit var maps: List<TNTWarsMap> private set
    lateinit var votes: HashMap<String, MapVote> private set

    private lateinit var menu: IMenu

    override fun create() {
        mapSelectorItem = makeItem(Material.BOOK) {
            named("&aMap Selector &7(Right Click)")
            withPersistentData(GenericItemListener.guiKey, name)
            withPersistentData(GenericItemListener.movableKey, PersistentDataType.BOOLEAN, false)
            withPersistentData(GenericItemListener.droppableKey, PersistentDataType.BOOLEAN, false)
        }

        val currentMap = GameManager.instance.currentMap
        val mapData = currentMap?.getMapData()
        maps = MapManager.instance.enabledElements.filter { it != mapData }.shuffled().take(3)
        votes = HashMap()

        menu = HopperMenu("Vote for the next map") {
            for (i in maps.indices) {
                val map = maps[i]
                val mapItem = makeItem(map.itemMaterial) {
                    named("&6&l${map.name}")
                    setLore {
                        line("&8${Textial.doubleArrowSymbol} &fGamemode: &6${map.gamemodeName}")
                        line("&8${Textial.doubleArrowSymbol} &fCreator: &6${map.creator}")
                        line("&8${Textial.doubleArrowSymbol} &fVotes: &60")
                    }
                }

                addButton(i * 2) {
                    displayItem = mapItem
                    onClick { event ->
                        if (votes[event.player.uniqueId.toString()] != null) {
                            Soundial.play(event.player, Soundial.Fail)
                            event.player.sendMessage(Textial.msg.parse("&cYou have already voted for a map"))
                        } else {
                            val count = getVoteCount(event.player)
                            votes[event.player.uniqueId.toString()] = MapVote(map, getVoteCount(event.player))
                            Soundial.play(event.player, Soundial.Success)
                            if (count == 1) {
                                event.player.sendMessage(Textial.msg.parse("You have voted for &p${map.name}"))
                            } else {
                                event.player.sendMessage(Textial.msg.parse("You gave &s${count} votes for &p${map.name}"))
                            }

                            editItem(mapItem) {
                                val lore = meta.lore() ?: return@editItem
                                lore[2] = Textial.msg.parse("&8${Textial.doubleArrowSymbol} &fVotes: &6${votes.values.sumOf { it.amount }}")
                                meta.lore(lore)
                            }

                            displayItem = mapItem
                        }
                    }
                }
            }
        }
    }

    override fun open(player: Player) {
        menu.open(player)
        Soundial.play(player, Soundial.UIOpen)
    }

    private fun getVoteCount(player: Player): Int {
        var value = 1

        for (permission in player.effectivePermissions.filter { it.permission.startsWith("cubedcraft.mapvotes.") }) {
            val num = permission.permission.substring("cubedcraft.mapvotes.".length)
            try {
                Debug.log("${player.name} is eligible for $num")
                value = num.toInt()
            } catch (exception: Exception) {
                Debug.error(Exception("num $num is not a valid number", exception))
            }
        }

        Debug.log("${player.name} has $value votes")
        return value
    }
}

data class MapVote(val map: TNTWarsMap, val amount: Int)


