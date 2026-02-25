package xyz.devcmb.playground.ui.scoreboard

import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard

interface IScoreboard {
    val id: String
    fun getObjectives(scoreboard: Scoreboard): Set<Objective>
}