package xyz.devcmb.playground.listeners

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.util.Format

class PlayerListeners : Listener {
    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        val player = event.player

        event.joinMessage(
            Component.text("[").color(NamedTextColor.GRAY)
                .append(Component.text("+").color(NamedTextColor.GREEN))
                .append(Component.text("] ").color(NamedTextColor.GRAY))
                .append(Format.formatPlayerName(player).color(NamedTextColor.WHITE))
        )

        player.foodLevel = 20
        player.saturation = 0f

        player.addPotionEffect(PotionEffect(
            PotionEffectType.HUNGER,
            Int.MAX_VALUE, 0,
            true,
            false,
            false
        ))

        val config = ParkourPlayground.plugin.config
        val position = config.getList("lobby.position")!!

        player.teleport(Location(
            Bukkit.getWorld(config.getString("lobby.world")!!),
            position.get(0) as Double,
            position.get(1) as Double,
            position.get(2) as Double
        ))
    }

    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        event.quitMessage(
            Component.text("[").color(NamedTextColor.GRAY)
                .append(Component.text("-").color(NamedTextColor.RED))
                .append(Component.text("] ").color(NamedTextColor.GRAY))
                .append(Format.formatPlayerName(event.player).color(NamedTextColor.WHITE))
        )
    }

    @EventHandler
    fun onPlayerHungerLoss(event: FoodLevelChangeEvent) {
        if(event.entity !is Player) return;
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerDamage(event: EntityDamageEvent) {
        if(event.entity !is Player) return

        if(event.damageSource.damageType == DamageType.FALL) {
            event.isCancelled = true
        }
    }
}