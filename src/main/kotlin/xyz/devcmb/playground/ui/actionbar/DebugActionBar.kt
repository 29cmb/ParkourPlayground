package xyz.devcmb.playground.ui.actionbar

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import xyz.devcmb.playground.Constants
import xyz.devcmb.playground.ControllerDelegate
import xyz.devcmb.playground.controllers.LoopController
import xyz.devcmb.playground.ui.UserInterfaceUtility

class DebugActionBar(val player: Player) : IActionBar {
    override val id: String = "debugActionBar"
    val loopController: LoopController = ControllerDelegate.getController("loopController") as LoopController

    override fun getComponent(): Component {
//        return Component.empty()
//            .append(
//                Component.text("Parkour Playground")
//                    .color(NamedTextColor.DARK_AQUA)
//                    .font(UserInterfaceUtility.fonts["normal"])
//            )
//            .append(UserInterfaceUtility.CenterText(
//                Component.text("v" + Constants.VERSION)
//                    .color(NamedTextColor.GRAY)
//                    .append(Component.text(" (indev)").color(NamedTextColor.GOLD))
//                    .font(UserInterfaceUtility.fonts["size10shift11"]),
//                10f,
//                20f
//            ))
//            .append(UserInterfaceUtility.CenterText(
//                Component.text("GameState: ")
//                    .color(NamedTextColor.GREEN)
//                    .append(Component.text(loopController.currentState.name).color(NamedTextColor.YELLOW))
//                    .font(UserInterfaceUtility.fonts["size10shift22"]),
//                10f,
//                20f
//            ))
        return UserInterfaceUtility.MultiLineCenteredText(
            10f,
            20f,
            Component.text("Parkour Playground")
                .color(NamedTextColor.DARK_AQUA)
                .font(UserInterfaceUtility.fonts["normal"]),
            Component.text("v" + Constants.VERSION)
                .color(NamedTextColor.GRAY)
                .append(Component.text(" (indev)").color(NamedTextColor.GOLD))
                .font(UserInterfaceUtility.fonts["size10shift11"]),
        )
    }
}