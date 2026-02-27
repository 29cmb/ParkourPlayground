package xyz.devcmb.playground.controllers

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import xyz.devcmb.playground.Constants
import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.annotations.Configurable
import xyz.devcmb.playground.annotations.Controller
import xyz.devcmb.playground.util.DebugUtil
import xyz.devcmb.playground.util.Format

@Controller("listenerController", priority = Controller.Priority.LOWEST)
class ListenerController : IController {
    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        val player = event.player

        player.inventory.clear()

        event.joinMessage(
            Component.text("[").color(NamedTextColor.GRAY)
                .append(Component.text("+").color(NamedTextColor.GREEN))
                .append(Component.text("] ").color(NamedTextColor.GRAY))
                .append(Format.formatPlayerName(player).color(NamedTextColor.WHITE))
        )

        player.foodLevel = 20
        player.saturation = 0f

        if(Constants.IS_DEVELOPMENT) {
            DebugUtil.subscribe(player, DebugUtil.DebugLogLevel.WARNING)
            player.sendMessage(
                Component.text("Developer mode is active. You have automatically be subscribed to the warning debug channel.")
                    .color(NamedTextColor.YELLOW)
            )
        }
    }


    @EventHandler
    fun onPlayerRespawnEvent(event: PlayerRespawnEvent) {
        val player = event.player
        player.foodLevel = 20
        player.saturation = 0f
    }

    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        event.quitMessage(
            Component.text("[").color(NamedTextColor.GRAY)
                .append(Component.text("-").color(NamedTextColor.RED))
                .append(Component.text("] ").color(NamedTextColor.GRAY))
                .append(Format.formatPlayerName(event.player).color(NamedTextColor.WHITE))
        )

        DebugUtil.loggingSubscriptions.remove(event.player)
    }

    @EventHandler
    fun onPlayerHungerLoss(event: FoodLevelChangeEvent) {
        if(event.entity !is Player) return;
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerDamage(event: EntityDamageEvent) {
        if(event.entity !is Player) return

        if(
            event.damageSource.damageType == DamageType.FALL
            || event.damageSource.damageType == DamageType.FIREWORKS
        ) {
            event.isCancelled = true
        }
    }

    override fun init() {
    }
}