package com.jeroenvdg.tntwars

import com.jeroenvdg.minigame_utilities.Debug
import com.jeroenvdg.minigame_utilities.Scheduler
import com.jeroenvdg.tntwars.game.GameManager
import com.jeroenvdg.tntwars.managers.achievements.AchievementsManager
import com.jeroenvdg.tntwars.managers.mapManager.MapManager
import com.jeroenvdg.tntwars.misc.PlayerDeathHelper
import com.jeroenvdg.tntwars.player.PlayerManager
import com.jeroenvdg.tntwars.services.ServiceManager
import com.jeroenvdg.minigame_utilities.commands.CommandHandler
import com.jeroenvdg.minigame_utilities.gui.MenuListener
import com.jeroenvdg.tntwars.commands.*
import com.jeroenvdg.tntwars.interfaces.*
import com.jeroenvdg.tntwars.listeners.*
import com.jeroenvdg.tntwars.managers.*
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class TNTWars : JavaPlugin() {

    companion object {
        lateinit var instance: TNTWars private set
    }

    lateinit var config: Config private set
    lateinit var ranksConfig: RanksConfig private set
    lateinit var services: ServiceManager private set
    lateinit var mapManager: MapManager private set
    lateinit var gameManager: GameManager private set
    lateinit var playerManager: PlayerManager private set
    lateinit var guiManager: GUIManager private set
    lateinit var schematicManager: SchematicManager private set
    lateinit var playerDeathHelper: PlayerDeathHelper private set
    lateinit var playerStatsManager: PlayerStatsManager private set
    lateinit var boosterManager: BoosterManager private set
    lateinit var achievementManager: AchievementsManager private set

    private var placeholderAPI: PlaceholderAPI? = null

    init {
        instance = this
    }

    override fun onEnable() {
        Debug.setup(this)
        Debug.log("Initializing helpers")
        Scheduler.setup(this)
        com.jeroenvdg.minigame_utilities.ItemBuilder.setup(this)
        Schematic.setup()
        playerDeathHelper = PlayerDeathHelper()

        Debug.log("Loading configs")
        saveDefaultConfig()
        config = Config.from(this.getConfig())
        saveResource("ranks.yml", false)
        ranksConfig = RanksConfig.from(YamlConfiguration.loadConfiguration(File(dataFolder, "ranks.yml")))

        Debug.log("Initializing services")
        services = ServiceManager()
        services.initServices()

        Debug.log("Loading world and map manager")
        val worldPath = "${server.worldContainer}${File.separator}maps"
        val worldManager = WorldManager(worldPath)
        mapManager = MapManager(worldManager)
        mapManager.loadAll()
        mapManager.cleanupLeftoverMaps()

        Debug.log("Creating managers")
        playerManager = PlayerManager()
        gameManager = GameManager(mapManager)
        guiManager = GUIManager()
        schematicManager = SchematicManager(this)
        playerStatsManager = PlayerStatsManager()
        boosterManager = BoosterManager()
        achievementManager = AchievementsManager()

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderAPI = PlaceholderAPI(this)
            placeholderAPI!!.register()
        }

        addEventListener(playerManager)
        addEventListener(BlockListener())
        addEventListener(BlockOwnershipManager(this))
        addEventListener(GenericItemListener())
        addEventListener(PlayerEventListener())
        addEventListener(TNTSpawnListener(this))
        addEventListener(MenuListener())

        Debug.log("Registering commands")
        addCommand(MapManagerCommand(mapManager))
        addCommand(GameCommand())
        addCommand(SettingsCommand())
        addCommand(ShopCommand())
        addCommand(TeamChatCommand())
        addCommand(MapCommand())
        addCommand(ProfileCommand())
        addCommand(TeamCommand())
        addCommand(BoosterCommand())

        Debug.log("Creating GUIs")
        recreateGuis()

        gameManager.activate()
        if (!gameManager.isActive) {
            Debug.log("TNTWars had no maps! Reload the plugin after enabling some")
            return
        }

        playerManager.repair()
        Debug.log("TNTWars is ready!")
    }

    override fun onDisable() {
        gameManager.deactivate()
        services.dispose()

        if (placeholderAPI != null) {
            placeholderAPI!!.unregister()
            placeholderAPI = null
        }

        while (playerManager.size > 0) {
            playerManager.removePlayer(playerManager.players.first().bukkitPlayer, false)
        }

        EventBus.reset()
        Debug.log("TNTWars is disabled!")
    }

    fun recreateGuis() {
        guiManager.clear()
        guiManager.add(TeamSelector())
        guiManager.add(ItemSelector())
        guiManager.add(MapSelector())
        guiManager.add(SettingsInterface())
        guiManager.add(ShopInterface())
        guiManager.add(ProfileInterface())
        guiManager.add(AchievementsInterface())
        guiManager.add(BoosterInterface())
    }

    fun addEventListener(listener : Listener) = server.pluginManager.registerEvents(listener, this)
    fun removeEventListener(listener: Listener) = HandlerList.unregisterAll(listener)
    private fun addCommand(command: CommandHandler) {
        val bukkitCommand = getCommand(command.name)!!
        bukkitCommand.setExecutor(command)
        bukkitCommand.tabCompleter = command
    }
}
