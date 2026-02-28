package xyz.devcmb.playground.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import kotlin.math.floor

object Format {
    fun formatPlayerName(player: Player, color: TextColor = NamedTextColor.WHITE) : Component {
        return Component.text(player.name).color(color)
    }

    fun formatTime(time: Int) : String {
        val minutes: Int = floor(time / 60.0).toInt()
        val seconds: Int = time % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}