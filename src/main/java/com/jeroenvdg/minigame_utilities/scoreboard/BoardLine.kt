package com.jeroenvdg.minigame_utilities.scoreboard

class BoardLine(val section: BoardSection, template: String) {

    val id = section.display.nextId()
    val team = section.display.scoreboard.registerNewTeam(id)
    val template = section.display.registerParams(template, ::updateParam)

    val params = HashMap<String, String>()

    init {
        team.prefix(section.display.textial.parse(this.template))
        team.addEntry(id)
    }

    fun setIndex(index: Int) {
        section.display.objective.getScore(id).score = index
    }


    fun remove() {
        team.removeEntry(id)
        team.unregister()
        section.display.scoreboard.resetScores(id)
    }


    private fun updateParam(param: String, value: String) {
        var copy = template
        params[param] = value
        for (p in params) { copy = copy.replace("[$param]", value) }
        team.prefix(section.display.textial.parse(copy))
    }
}