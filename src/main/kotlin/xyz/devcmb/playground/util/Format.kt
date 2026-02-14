package xyz.devcmb.playground.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import kotlin.math.floor

object Format {
    fun format(string: String, type: FormatType): Component {
        val data = type.data
        return Component.text(
            data["prefix"].toString()
        ).append(
            Component.text(" ")
                .append(Component.text(string).color(data["color"] as NamedTextColor?))
        )
    }

    fun format(component: Component, type: FormatType): Component {
        val data = type.data
        return Component.text(
            data["prefix"].toString()
        ).append(
            Component.text(" ")
                .append(component)
        )
    }

    fun formatPlayerName(player: Player) : Component {
        return Component.text(player.name)
    }

    fun formatTime(time: Int) : String {
        val minutes: Int = floor(time / 60.0).toInt()
        val seconds: Int = time % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    enum class FormatType(val data: MutableMap<String?, Any?>) {
        SUCCESS(
            hashMapOf(
                "prefix" to "✅",
                "color" to NamedTextColor.GREEN
            )
        ),
        WARNING(
            hashMapOf(
                "prefix" to "⚠️",
                "color" to NamedTextColor.YELLOW
            )
        ),
        ERROR(
            hashMapOf(
                "prefix" to "❌",
                "color" to NamedTextColor.DARK_RED
            )
        ),
        INVALID(
            hashMapOf(
                "prefix" to "❓",
                "color" to NamedTextColor.RED
            )
        ),
        INFO(
            hashMapOf(
                "prefix" to "ℹ️",
                "color" to NamedTextColor.AQUA
            )
        )
    }
}