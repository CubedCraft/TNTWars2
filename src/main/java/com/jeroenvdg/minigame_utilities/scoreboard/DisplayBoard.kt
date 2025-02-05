package com.jeroenvdg.minigame_utilities.scoreboard

import com.jeroenvdg.minigame_utilities.Textial
import com.jeroenvdg.minigame_utilities.TextialParser
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot

class DisplayBoard(val player: Player) {

    val scoreboard = Bukkit.getScoreboardManager().newScoreboard
    val objective = scoreboard.registerNewObjective(player.uniqueId.toString(), Criteria.DUMMY, Component.empty())

    var textial = TextialParser("", Textial.Gold, Textial.Green, Textial.Red, Textial.White); private set

    private var titleTemplate = ""
    private val sections = ArrayList<BoardSection>()
    private val params = HashMap<String, (param: String, value: String) -> Unit>()
    private var idIndex = 0


    init {
        player.scoreboard = scoreboard
        objective.displaySlot = DisplaySlot.SIDEBAR
    }


    fun setTextial(parser: TextialParser): DisplayBoard {
        this.textial = parser
        return this
    }


    fun setTitle(template: String): DisplayBoard {
        unregisterParams(titleTemplate)
        val params = HashMap<String, String>()
        titleTemplate = registerParams(template) { param, value ->
            params[param] = value
            var copy = titleTemplate
            for (p in params) { copy = copy.replace("[$param]", value) }
            objective.displayName(textial.parse(copy))
        }
        objective.displayName(textial.parse(titleTemplate))
        return this
    }


    fun registerSection(id: String, displayName: String, priority: Int, action: (BoardSection) -> Unit): DisplayBoard {
        val section = BoardSection(this, id, displayName, priority, action)
        sections.add(section)
        recalculateIndexes()
        return this
    }


    fun removeSection(id: String): DisplayBoard {
        val section = sections.find { it.id == id } ?: return this
        section.clear()
        sections.remove(sections.find { it.id == id })
        recalculateIndexes()
        return this
    }


    fun setParam(param: String, value: String) {
        params[param.lowercase()]?.invoke(param.lowercase(), value)
    }
    
    
    fun registerParams(template: String, onChange: (param: String, value: String) -> Unit): String {
        val regex = Regex("\\[(\\w+)\\]")
        var str = template

        for (matchResult in regex.findAll(template)) {
            val paramName = matchResult.groups[1]!!.value.lowercase()
            str = str.replace("[${matchResult.value}]", "[$paramName]")
            params[paramName] = onChange
        }

        return str
    }


    fun unregisterParams(template: String): String {
        val regex = Regex("\\[([\\w+])\\]")
        var str = template

        for (matchResult in regex.findAll(template)) {
            val paramName = matchResult.value.lowercase()
            params.remove(paramName)
        }

        return str
    }


    fun recalculateIndexes() {
        var index = 0
        for (section in sections.sortedBy { it.priority }) {
            for (line in section.lines.reversed()) {
                line.setIndex(++index)
            }
        }
    }


    fun nextId(): String {
        return textial.str(Integer.toHexString(idIndex++).map { "&$it" }.joinToString(""))
    }
}