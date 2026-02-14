package xyz.devcmb.playground.ui.bossbar

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import xyz.devcmb.playground.ControllerDelegate
import xyz.devcmb.playground.controllers.LoopController
import xyz.devcmb.playground.ui.UserInterfaceUtility

class DebugInfoBossBar(val player: Player) : IBossBar {
    override val id: String = "debugInfoBossBar"
    override val height: Int = 3

    override fun getComponent(): Component {
        val loopController = ControllerDelegate.getController("loopController") as LoopController
        return Component.text("GameState: ", NamedTextColor.AQUA)
            .append(Component.text(loopController.currentState.name, NamedTextColor.WHITE))
            .font(UserInterfaceUtility.fonts["size20shift10"])
    }
}