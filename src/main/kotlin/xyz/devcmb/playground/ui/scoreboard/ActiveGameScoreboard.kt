package xyz.devcmb.playground.ui.scoreboard

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import xyz.devcmb.playground.ControllerDelegate
import xyz.devcmb.playground.controllers.LoopController
import xyz.devcmb.playground.controllers.ObstacleController
import xyz.devcmb.playground.ui.UserInterfaceUtility
import xyz.devcmb.playground.util.Format
import xyz.devcmb.playground.util.MiscUtils
import java.util.*


class ActiveGameScoreboard(val player: Player) : IScoreboard {
    override val id: String = "activeGameScoreboard"
    override fun getObjectives(scoreboard: Scoreboard): Set<Objective> {
        val objective = scoreboard.registerNewObjective(
            "activeGameBoard",
            Criteria.create("dummy"),
            Component.text("Parkour Playground", NamedTextColor.DARK_AQUA).font(UserInterfaceUtility.fonts["normal"])
        )

        objective.displaySlot = DisplaySlot.SIDEBAR

        val loopController = ControllerDelegate.getController("loopController") as LoopController
        val obstacleController = ControllerDelegate.getController("obstacleController") as ObstacleController

        val sortedScores = loopController.playerScores.entries.sortedByDescending { (_, value) -> value }
        val playerPlacement = sortedScores.indexOfFirst { it.key == player } + 1

        val leaderboard: ArrayList<Component> = arrayListOf()
        val lastIndex = sortedScores.lastIndex

        when {
            playerPlacement <= 1 -> {
                for (i in 0..minOf(2, lastIndex)) {
                    val score = sortedScores[i]
                    leaderboard.add(
                        Component.empty()
                            .append(Component.text("#${i + 1} ", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                            .append(Format.formatPlayerName(score.key))
                            .append(Component.text(".".repeat(40), NamedTextColor.GRAY))
                            .append(Component.text(score.value, NamedTextColor.GOLD))
                    )
                }
            }

            playerPlacement >= lastIndex -> {
                for (i in maxOf(0, lastIndex - 1)..lastIndex) {
                    val score = sortedScores[i]
                    leaderboard.add(
                        Format.formatPlayerName(score.key)
                            .append(Component.text(".".repeat(20), NamedTextColor.GRAY))
                            .append(Component.text(score.value, NamedTextColor.GOLD))
                    )
                }
            }

            else -> {
                for (i in (playerPlacement - 1)..(playerPlacement + 1)) {
                    val score = sortedScores[i]
                    leaderboard.add(
                        Format.formatPlayerName(score.key)
                            .append(Component.text(".".repeat(20), NamedTextColor.GRAY))
                            .append(Component.text(score.value, NamedTextColor.GOLD))
                    )
                }
            }
        }

        MiscUtils.addScoreboardObjectiveLines(objective, arrayListOf(
            Component.empty(),
            Component.text("Crumble speed: ", NamedTextColor.WHITE)
                .append(Component.text("${obstacleController.currentCrumbleSpeedMultiplier}x", NamedTextColor.YELLOW))
                .font(UserInterfaceUtility.fonts["normal"]),
            Component.empty(),
            Component.text("Your score: ", NamedTextColor.WHITE)
                .append(Component.text(loopController.playerScores.get(player) ?: 0, NamedTextColor.GOLD))
                .font(UserInterfaceUtility.fonts["normal"]),
            Component.text("Obstacles completed: ", NamedTextColor.WHITE)
                .append(Component.text(loopController.playerObstacleCounts.get(player) ?: 0, NamedTextColor.GREEN))
                .font(UserInterfaceUtility.fonts["normal"]),
            Component.empty(),
            *leaderboard.toTypedArray(),
            Component.empty()
        ))

        return setOf(objective)
    }
}