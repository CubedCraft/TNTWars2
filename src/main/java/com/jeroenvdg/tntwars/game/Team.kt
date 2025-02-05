package com.jeroenvdg.tntwars.game

import com.jeroenvdg.tntwars.TNTWars
import com.jeroenvdg.minigame_utilities.Textial

enum class Team(val primaryColor: Textial, val isSpectatorTeam: Boolean) {
    Spectator(Textial.Aqua, true),
    Queue(Textial.Gold, true),
    Red(Textial.Red, false),
    Blue(Textial.Blue, false);

    val isGameTeam get() = !isSpectatorTeam
    fun usersInTeam() = TNTWars.instance.playerManager.findUsersInTeam(this)
}

