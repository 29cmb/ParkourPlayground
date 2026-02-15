package xyz.devcmb.playground.ui.bossbar

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import xyz.devcmb.playground.ControllerDelegate
import xyz.devcmb.playground.controllers.LoopController
import xyz.devcmb.playground.ui.UserInterfaceUtility
import xyz.devcmb.playground.util.Format

class GameStateBossBar(val player: Player) : IBossBar {
    override val id: String = "gameStateBossBar"
    override val height: Int = 3
    override fun getComponent(): Component {
        val loopController = ControllerDelegate.getController("loopController") as LoopController
        return when(loopController.currentState) {
            LoopController.GameState.PLAYER_WAITING ->
                Component.text("Waiting for players")
            LoopController.GameState.INTERMISSION ->
                Component.text("Intermission - ${Format.formatTime(loopController.countdown)}")
            LoopController.GameState.PREPARING_WORLD ->
                Component.text("Loading world...")


            LoopController.GameState.PAUSED ->
                Component.text("Paused", NamedTextColor.YELLOW)
            LoopController.GameState.ERROR ->
                Component.text("Error - Check Console")
            else -> Component.text("Loading...")
        }.font(UserInterfaceUtility.fonts["size16shift6"])
    }
}