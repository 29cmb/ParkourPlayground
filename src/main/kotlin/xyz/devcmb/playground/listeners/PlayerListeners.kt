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
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.annotations.Configurable
import xyz.devcmb.playground.util.Format

class PlayerListeners : Listener {
    companion object {
        @Configurable("lobby.position")
        var lobbySpawn: List<Double> = listOf(0.5,67.0,0.5)

        @Configurable("lobby.world")
        var lobbyWorld: String = "hub"
    }

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

        if(player.vehicle != null) {
            player.vehicle!!.remove()
        }

        player.teleport(Location(
            Bukkit.getWorld(lobbyWorld),
            lobbySpawn.get(0),
            lobbySpawn.get(1),
            lobbySpawn.get(2)
        ))
    }


    @EventHandler
    fun onPlayerRespawnEvent(event: PlayerRespawnEvent) {
        val player = event.player
        player.foodLevel = 20
        player.saturation = 0f

        player.teleport(Location(
            Bukkit.getWorld(lobbyWorld),
            lobbySpawn.get(0),
            lobbySpawn.get(1),
            lobbySpawn.get(2)
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