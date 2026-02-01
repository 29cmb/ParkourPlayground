package xyz.devcmb.playground.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import java.util.Map

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

    enum class FormatType(val data: MutableMap<String?, Any?>) {
        SUCCESS(
            Map.of<String?, Any?>(
                "prefix", "✅",
                "color", NamedTextColor.GREEN
            )
        ),
        WARNING(
            Map.of<String?, Any?>(
                "prefix", "⚠️",
                "color", NamedTextColor.YELLOW
            )
        ),
        ERROR(
            Map.of<String?, Any?>(
                "prefix", "❌",
                "color", NamedTextColor.DARK_RED
            )
        ),
        INVALID(
            Map.of<String?, Any?>(
                "prefix", "❓",
                "color", NamedTextColor.RED
            )
        ),
        INFO(
            Map.of<String?, Any?>(
                "prefix", "ℹ️",
                "color", NamedTextColor.AQUA
            )
        )
    }
}