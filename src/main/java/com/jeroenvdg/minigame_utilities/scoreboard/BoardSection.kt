package com.jeroenvdg.minigame_utilities.scoreboard

import com.jeroenvdg.minigame_utilities.Textial

class BoardSection(val display: DisplayBoard, val id: String, template: String, val priority: Int, action: (BoardSection) -> Unit) {

    val lines = ArrayList<BoardLine>()
    val team = display.scoreboard.registerNewTeam(id)


    init {
        lines.add(BoardLine(this, template))
        action(this)
    }


    fun addLine(template: String): BoardSection {
        if (template == "") {
            lines.add(BoardLine(this, ""))
        } else {
            lines.add(BoardLine(this, "&8${Textial.doubleArrowSymbol} &r$template"))
        }
        return this
    }


    fun clear() {
        for (line in lines) {
            line.remove()
        }
        lines.clear()
    }
}