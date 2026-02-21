package xyz.devcmb.playground.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import xyz.devcmb.playground.ParkourPlayground
import java.util.TimerTask
import kotlin.math.min

object MiscUtils {
    fun delay(seconds: Int, execute: () -> Unit): BukkitRunnable {
        val runnable = object : BukkitRunnable() {
            override fun run() = execute()
        }

        runnable.runTaskLater(ParkourPlayground.plugin, seconds * 20L)
        return runnable
    }

    fun countdown(players: Set<Player>, seconds: Int, onFinish: () -> Unit) {
        var timeLeft = seconds

        object : BukkitRunnable() {
            override fun run() {
                timeLeft -= 1
                if(timeLeft <= 0) {
                    this.cancel()
                    onFinish()
                    return
                }

                players.forEach {
                    val title = Title.title(
                        Component.text(">${" ".repeat(min(4, timeLeft))}")
                            .append(Component.text(timeLeft.toString()))
                            .append(Component.text("${" ".repeat(min(4, timeLeft))}<"))
                            .color(when(timeLeft) {
                                1 -> NamedTextColor.RED
                                2 -> NamedTextColor.YELLOW
                                3 -> NamedTextColor.GREEN
                                else -> NamedTextColor.WHITE
                            })
                            .decorate(TextDecoration.BOLD),
                        Component.text("The game is about to begin!"),
                        Title.Times.times(Ticks.duration(0), Ticks.duration(25), Ticks.duration(0))
                    )

                    it.showTitle(title)
                }
            }
        }.runTaskTimer(ParkourPlayground.plugin, 0, 20L)
    }
}
