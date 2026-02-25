package xyz.devcmb.playground.controllers

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.world.block.BlockType
import com.sk89q.worldedit.world.block.BlockTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import org.bukkit.*
import org.bukkit.damage.DamageType
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import xyz.devcmb.playground.ControllerDelegate
import xyz.devcmb.playground.ObstacleStepException
import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.annotations.Configurable
import xyz.devcmb.playground.annotations.Controller
import xyz.devcmb.playground.ui.UserInterfaceUtility
import xyz.devcmb.playground.util.DebugUtil
import xyz.devcmb.playground.util.MiscUtils
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.io.path.Path
import kotlin.math.max
import kotlin.math.min

@Controller("obstacleController", Controller.Priority.HIGH)
class ObstacleController : IController {
    val loadedObstacles: ArrayList<LoadedObstacle> = ArrayList()
    val playerObstacles: HashMap<Player, UUID> = HashMap()
    val playerSpawns: HashMap<Player, Location> = HashMap()

    companion object {
        @field:Configurable("templates.root_path")
        var templateRootPath: String = "templates"

        @field:Configurable("templates.obstacles_path")
        var obstaclesPath: String = "obstacles"

        @field:Configurable("game.starting_obstacle_pivot")
        var startingObstaclePivot: List<Int> = listOf(-1,65,8)

        @field:Configurable("game.starting_position")
        var startPosition: List<Double> = listOf(-0.5, 67.0, -0.5)

        @field:Configurable("game.min_y_fallback")
        var minYFallback: Int = 30

        @field:Configurable("game.y_lenience")
        var yLenience: Int = 10
    }

    override fun init() {
    }

    fun pregame(loopController: LoopController) {
        playerSpawns.clear()
        playerObstacles.clear()
        loadedObstacles.clear()

        Bukkit.getOnlinePlayers().forEach {
            playerSpawns.put(it, Location(
                loopController.world,
                startPosition[0],
                startPosition[1],
                startPosition[2]
            ))
        }
    }

    fun stepObstacleLoad(type: ObstacleType? = null) {
        val loopController = ControllerDelegate.getController("loopController") as LoopController
        if(loopController.world == null) {
            throw ObstacleStepException("Cannot step load cycle while game is not on")
        }

        val type = type ?: ObstacleType.values().random()

        val loadPosition: BlockVector3 =
            loadedObstacles.lastOrNull()?.endPos ?:
                BlockVector3.at(
                    startingObstaclePivot[0],
                    startingObstaclePivot[1],
                    startingObstaclePivot[2]
                )

        var obstacle: File
        try {
            obstacle = getRandomObstacle(type)
        } catch(e: IllegalStateException) {
            throw ObstacleStepException("Failed to get an obstacle with type ${type.name}: ${e.message}")
        }

        var clipboard: Clipboard
        try {
            loadObstacleFromFile(obstacle, loadPosition, loopController.world!!, false, { clipboard, pos ->
                val endPivot = getPivotLine(clipboard, BlockTypes.REDSTONE_BLOCK!!)!!
                val pivot = clipboard.origin

                val worldEndPos = loadPosition.add(
                    endPivot.x() - pivot.x(),
                    endPivot.y() - pivot.y(),
                    endPivot.z() - pivot.z()
                )

                val region = clipboard.region

                var minX = Int.MAX_VALUE
                var minY = Int.MAX_VALUE
                var minZ = Int.MAX_VALUE

                var maxX = Int.MIN_VALUE
                var maxY = Int.MIN_VALUE
                var maxZ = Int.MIN_VALUE

                var found = false

                for (pos in region) {
                    val block = clipboard.getBlock(pos)

                    if (block.blockType == BlockTypes.AIR!!) continue

                    found = true

                    minX = min(minX, pos.x())
                    minY = min(minY, pos.y())
                    minZ = min(minZ, pos.z())

                    maxX = max(maxX, pos.x())
                    maxY = max(maxY, pos.y())
                    maxZ = max(maxZ, pos.z())
                }

                val offset = loadPosition.subtract(clipboard.origin)

                val min = BlockVector3.at(minX, minY, minZ).add(offset)
                val max = BlockVector3.at(maxX, maxY, maxZ).add(offset)

                loadedObstacles.add(
                    LoadedObstacle(
                        UUID.randomUUID(),
                        obstacle,
                        type,
                        pos,
                        worldEndPos,
                        min,
                        max
                    )
                )
            })
        } catch(e: IllegalStateException) {
            throw ObstacleStepException("Failed to load obstacle ${obstacle.path.toString()}: ${e.message}")
            return
        }
    }

    fun getRandomObstacle(type: ObstacleType): File {
        val parent = File(templateRootPath, obstaclesPath)
        if (!parent.exists() || !parent.isDirectory) throw IllegalStateException("Root obstacles path not found (${parent.path.toString()}) or not a directory, no obstacles can exist")

        val typeFolder = File(parent, type.name.lowercase())
        if(!typeFolder.exists() || !typeFolder.isDirectory) throw IllegalStateException("Folder for obstacle type ${type.name} not found or not a directory.")

        return typeFolder.listFiles().random()
    }

    fun saveObstacle(clipboard: Clipboard, name: String, type: ObstacleType, onSuccess: () -> Unit, onError: (err: String) -> Unit) {
        val saveDirectory = File(Path(templateRootPath, obstaclesPath, type.name.lowercase()).toString())
        if(!saveDirectory.exists()) {
            saveDirectory.mkdirs()
        }

        if(getPivotLine(clipboard, BlockTypes.DIAMOND_BLOCK!!) == null) {
            onError("A row of 5 diamond blocks indicating the start of the segment was not found in the schematic!")
            return
        }

        if(getPivotLine(clipboard, BlockTypes.REDSTONE_BLOCK!!) == null) {
            onError("A row of 5 redstone blocks indicating the end of the segment was not found in the schematic!")
            return
        }

        try {
            FileOutputStream(File(saveDirectory, "$name.schem")).use { outputStream ->
                BuiltInClipboardFormat.FAST_V3
                    .getWriter(outputStream)
                    .use { writer -> writer.write(clipboard) }
            }
            DebugUtil.success("Saved obstacle to ${type.name.lowercase()}/$name.schem successfully")
            onSuccess()
        } catch(e: Exception) {
            onError("An error occurred while trying to save the schematic: ${e.message ?: " Unknown error"}")
        }
    }

    // This logic was human-made then made look pretty by chatgpt
    private fun getPivotLine(clipboard: Clipboard, type: BlockType): BlockVector3? {
        for (origin in clipboard.region) {
            if (clipboard.getBlock(origin).blockType != type) continue

            fun check(dx: Int, dz: Int): BlockVector3? {
                for (i in -2..2) {
                    val pos = BlockVector3.at(
                        origin.x() + dx * i,
                        origin.y(),
                        origin.z() + dz * i
                    )

                    if (!clipboard.region.contains(pos)) return null
                    if (clipboard.getBlock(pos).blockType != type) return null
                }
                return origin
            }

            val xCheck = check(1,0)
            val zCheck = check(0,1)

            if(xCheck != null || zCheck != null) return xCheck ?: zCheck
        }

        return null
    }

    // is the reuse redundant
    // uh
    private fun getPivotAxis(clipboard: Clipboard, type: BlockType): Axis? {
        for (origin in clipboard.region) {
            if (clipboard.getBlock(origin).blockType != type) continue

            fun check(dx: Int, dz: Int): Boolean {
                for (i in -2..2) {
                    val pos = BlockVector3.at(
                        origin.x() + dx * i,
                        origin.y(),
                        origin.z() + dz * i
                    )

                    if (!clipboard.region.contains(pos)) return false
                    if (clipboard.getBlock(pos).blockType != type) return false
                }
                return true
            }

            if(check(1,0)) {
                return Axis.X
            }

            if(check(0,1)) {
                return Axis.Z
            }
        }

        return null
    }

    fun loadObstacleFromFile(file: File, position: BlockVector3, world: World, force: Boolean, onComplete: (clipboard: Clipboard, pos: BlockVector3) -> Unit = {clipboard,pos ->}) {
        val format = ClipboardFormats.findByFile(file)
            ?: throw IllegalArgumentException("Unknown schematic format")

        val clipboard: Clipboard
        format.getReader(file.inputStream()).use { reader ->
            clipboard = reader.read()
        }

        val region = clipboard.region
        val origin = clipboard.origin

        var pivot = getPivotLine(clipboard, BlockTypes.DIAMOND_BLOCK!!)
        var position = position
        if(pivot == null) {
            if(force) DebugUtil.warning("Loading obstacle from path ${file.path.toString()} without a valid start")
            else throw IllegalStateException("Cannot load obstacle without a start pivot of 5 diamond blocks")
        } else {
            val axis = getPivotAxis(clipboard, BlockTypes.DIAMOND_BLOCK!!)
            DebugUtil.info("Found that the diamond line is along the ${axis} axis")

            // Its inverted because if the line is on the X axis, forward and backward is the Z axis
            position = position.add(
                if (axis == Axis.Z) 1 else 0,
                0,
                if(axis == Axis.X) 1 else 0
            )

            clipboard.origin = pivot
        }

        if(getPivotLine(clipboard, BlockTypes.REDSTONE_BLOCK!!) == null) {
            if(force) DebugUtil.warning("Loading obstacle from path ${file.path.toString()} without a valid end")
            else throw IllegalStateException("Cannot load obstacle without an end pivot of 5 redstone blocks")
        }

        val min = region.minimumPoint.subtract(origin).add(position)
        val max = region.maximumPoint.subtract(origin).add(position)

        for (x in (min.x() shr 4)..(max.x() shr 4)) {
            for (z in (min.z() shr 4)..(max.z() shr 4)) {
                world.getChunkAt(x, z).load(true)
            }
        }

        val editSession = WorldEdit.getInstance()
            .newEditSessionBuilder()
            .world(BukkitAdapter.adapt(world))
            .fastMode(true)
            .build()

        val operation = ClipboardHolder(clipboard)
            .createPaste(editSession)
            .to(position)
            .ignoreAirBlocks(false)
            .build()

        Operations.complete(operation)
        editSession.flushQueue()
        editSession.close()

        DebugUtil.success("Loaded obstacle ${file.parentFile.name}/${file.name} successfully")

        for (pos in clipboard.region) {
            val block = clipboard.getBlock(pos)

            if (block.blockType == BlockTypes.AIR) continue

            val worldX = position.x() + (pos.x() - origin.x())
            val worldY = position.y() + (pos.y() - origin.y())
            val worldZ = position.z() + (pos.z() - origin.z())

            val pos = Location(world, worldX + 0.5, worldY + 0.5, worldZ + 0.5)
            val blockData = world.getBlockData(worldX, worldY, worldZ)
            world.spawnParticle(
                Particle.BLOCK,
                pos,
                20,
                0.0,
                0.0,
                0.0,
                blockData
            )

            world.playSound(pos, blockData.soundGroup.breakSound, SoundCategory.BLOCKS, 1f, 1f)
        }

        onComplete(clipboard, position)
    }

    @EventHandler
    fun playerObstacleHandle(event: PlayerMoveEvent) {
        val player = event.player
        val loc = player.location

        val loopController = ControllerDelegate.getController("loopController") as LoopController
        if(player.world != loopController.world) return

        val currentObstacle = loadedObstacles.find { obstacle ->
            loc.blockX in obstacle.boundsMin.x()..obstacle.boundsMax.x() &&
            loc.blockY in obstacle.boundsMin.y()..obstacle.boundsMax.y() &&
            loc.blockZ in obstacle.boundsMin.z()..obstacle.boundsMax.z()
        }

        if (currentObstacle != null) {
            val obstacleIndex = loadedObstacles.indexOfFirst { it.id == currentObstacle.id }
            val lastObstacle = playerObstacles.get(player)
            if(lastObstacle != currentObstacle.id) {
                val lastObstacleIndex = loadedObstacles.indexOfFirst { it.id == lastObstacle }
                if(lastObstacleIndex > obstacleIndex) return

                DebugUtil.info("Player ${player.name} entered a different obstacle with type ${currentObstacle.type.name}")

                val x = currentObstacle.startPos.x().toDouble() + 0.5
                val y = currentObstacle.startPos.y() + 2.0
                val z = currentObstacle.startPos.z().toDouble() + 0.5
                playerSpawns.put(player, Location(
                    player.world,
                    x,
                    y,
                    z
                ))

                DebugUtil.info("Set respawn point for ${player.name} to $x, $y, $z")

                val loadedLastObstacle = loadedObstacles.find { it.id  == lastObstacle }
                val lastObstacleType = loadedLastObstacle?.type ?: ObstacleType.NORMAL
                var subtitle = Component.empty()

                if(lastObstacleType != currentObstacle.type) {
                    player.inventory.clear()
                    when(currentObstacle.type) {
                        ObstacleType.ELYTRA -> {
                            player.inventory.chestplate = ItemStack.of(Material.ELYTRA).apply {
                                val meta = itemMeta
                                meta.isUnbreakable = true
                                itemMeta = meta
                            }
                        }
                        ObstacleType.TRIDENT -> {
                            player.inventory.setItemInMainHand(ItemStack.of(Material.TRIDENT).apply {
                                val meta = itemMeta
                                meta.isUnbreakable = true
                                meta.addEnchant(Enchantment.RIPTIDE, 1, false)
                                itemMeta = meta
                            })
                        }
                        ObstacleType.NORMAL -> {}
                        ObstacleType.WIND_CHARGE -> {
                            player.inventory.setItemInMainHand(ItemStack.of(Material.WIND_CHARGE, 64))
                        }
                    }

                    if(lastObstacleType.icon !== null) {
                        subtitle = subtitle.append(
                            Component.text("- [").append(
                                Component.text(lastObstacleType.icon)
                                    .font(UserInterfaceUtility.fonts["icons"])
                                    .color(NamedTextColor.WHITE)
                            ).append(Component.text("]")).color(NamedTextColor.RED)
                        )
                    }

                    if(currentObstacle.type.icon !== null) {
                        if(lastObstacleType.icon !== null) {
                            subtitle = subtitle.append(Component.text(" "))
                        }

                        subtitle = subtitle.append(
                            Component.text("+ [").append(
                                Component.text(currentObstacle.type.icon)
                                    .font(UserInterfaceUtility.fonts["icons"])
                                    .color(NamedTextColor.WHITE)
                            ).append(Component.text("]")).color(NamedTextColor.GREEN)
                        )
                    }
                }

                var mainTitle = Component.empty()
                if(lastObstacle != null) {
                    val score = loopController.addPlayerObstacleScore(player)

                    loopController.playerObstacleCounts[player] =
                        if(loopController.playerObstacleCounts.containsKey(player)) loopController.playerObstacleCounts[player]!! + 1
                        else 1

                    mainTitle = Component.text("+${score}", NamedTextColor.GOLD)
                    player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_CHIME, 10f, 2f)
                    MiscUtils.spawnPrivateFirework(
                        player,
                        FireworkEffect.builder()
                            .trail(false)
                            .flicker(false)
                            .withColor(Color.YELLOW)
                            .withColor(Color.ORANGE)
                            .with(FireworkEffect.Type.BALL_LARGE)
                            .build()
                    )
                }

                val title = Title.title(
                    mainTitle,
                    subtitle,
                    Title.Times.times(
                        Ticks.duration(5),
                        Ticks.duration(40),
                        Ticks.duration(5)
                    )
                )

                player.showTitle(title)
            }

            playerObstacles.put(player, currentObstacle.id)

            if(obstacleIndex >= loadedObstacles.size - 3) {
                stepObstacleLoad()
            }
        }
    }

    @EventHandler
    fun playerFallEvent(event: PlayerMoveEvent) {
        val player = event.player
        val loc = player.location

        val loopController = ControllerDelegate.getController("loopController") as LoopController
        if(player.world != loopController.world) return

        val currentObstacle = loadedObstacles.find { it.id == playerObstacles.get(player) }
        if(currentObstacle == null) {
            if(loc.y < minYFallback) {
                respawnPlayer(player)
            }
            return
        }

        if(loc.y < currentObstacle.boundsMin.y() - yLenience) {
            respawnPlayer(player)
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun playerDamageEvent(event: EntityDamageEvent) {
        if(event.isCancelled) return

        val player = event.entity
        if(player !is Player) return

        val loopController = ControllerDelegate.getController("loopController") as LoopController
        if(player.world != loopController.world) return

        val damageSource = event.damageSource
        if(
            damageSource.damageType == DamageType.FALL
            || damageSource.damageType == DamageType.TRIDENT
            || damageSource.damageType == DamageType.FIREWORKS
        ) return

        event.isCancelled = true
        respawnPlayer(player)
    }

    // https://github.com/666pyke/NoWindCharge/blob/main/src/main/java/org/me/pyke/nowindcharge/WindChargeListener.java
    @EventHandler
    fun playerWindChargeEvent(event: PlayerInteractEvent) {
        val player = event.player

        val mainHandItem: ItemStack = player.inventory.getItemInMainHand()
        val offHandItem: ItemStack = player.inventory.getItemInOffHand()

        if ((mainHandItem.getType() != Material.WIND_CHARGE && offHandItem.getType() != Material.WIND_CHARGE) ||
            (event.getAction() !== Action.RIGHT_CLICK_AIR && event.getAction() !== Action.RIGHT_CLICK_BLOCK)
        ) {
            return
        }

        Bukkit.getScheduler().runTask(ParkourPlayground.plugin, Runnable {
            val main = player.inventory.itemInMainHand
            val off = player.inventory.itemInOffHand

            if (main.type == Material.WIND_CHARGE) {
                main.amount = 64
            }

            if (off.type == Material.WIND_CHARGE) {
                off.amount = 64
            }
        })
    }

    fun respawnPlayer(player: Player) {
        player.fallDistance = 0f
        player.teleport(playerSpawns.get(player)!!)
    }

    data class LoadableObstacle(val schematic: File)
    data class LoadedObstacle(
        val id: UUID,
        val schematic: File,
        val type: ObstacleType,
        val startPos: BlockVector3,
        val endPos: BlockVector3,
        val boundsMin: BlockVector3,
        val boundsMax: BlockVector3
    )

    enum class ObstacleType(val icon: String?) {
        NORMAL(null),
        TRIDENT("\uE001"),
        ELYTRA("\uE000"),
        WIND_CHARGE("\uE002"),
    }
}