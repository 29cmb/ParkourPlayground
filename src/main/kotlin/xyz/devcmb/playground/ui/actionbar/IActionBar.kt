package xyz.devcmb.playground.ui.actionbar

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

interface IActionBar {
    val id: String
    fun getComponent(): Component
}