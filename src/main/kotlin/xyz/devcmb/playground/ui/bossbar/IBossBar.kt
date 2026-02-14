package xyz.devcmb.playground.ui.bossbar

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component

interface IBossBar {
    val id: String
    // The amount of bossbars to reserve (to prevent overlap if multiple are visible)
    val height: Int
    fun getComponent(): Component
}