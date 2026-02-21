package xyz.devcmb.playground.controllers

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.world.block.BlockType
import com.sk89q.worldedit.world.block.BlockTypes
import org.bukkit.World
import xyz.devcmb.playground.ControllerDelegate
import xyz.devcmb.playground.ObstacleStepException
import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.annotations.Configurable
import xyz.devcmb.playground.annotations.Controller
import java.io.File
import java.io.FileOutputStream
import kotlin.io.path.Path

@Controller("obstacleController", Controller.Priority.HIGH)
class ObstacleController : IController {
    val loadedObstacles: ArrayList<LoadedObstacle> = ArrayList()

    companion object {
        @field:Configurable("templates.root_path")
        var templateRootPath: String = "templates"

        @field:Configurable("templates.obstacles_path")
        var obstaclesPath: String = "obstacles"

        @field:Configurable("game.starting_obstacle_pivot")
        var startingObstaclePivot: List<Int> = listOf(-1,65,8)
    }

    override fun init() {
    }

    fun stepObstacleLoad(type: ObstacleType?) {
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
            clipboard = loadObstacleFromFile(obstacle, loadPosition, loopController.world!!, false)
        } catch(e: IllegalStateException) {
            throw ObstacleStepException("Failed to load obstacle ${obstacle.path.toString()}: ${e.message}")
            return
        }

        val endPivot = getPivotLine(clipboard, BlockTypes.REDSTONE_BLOCK!!)!!
        val pivot = clipboard.origin

        val worldEndPos = loadPosition.add(
            endPivot.x() - pivot.x(),
            endPivot.y() - pivot.y(),
            endPivot.z() - pivot.z()
        )

        loadedObstacles.add(
            LoadedObstacle(obstacle, type, loadPosition, worldEndPos)
        )
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
            FileOutputStream(File(saveDirectory, name)).use { outputStream ->
                BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC
                    .getWriter(outputStream)
                    .use { writer -> writer.write(clipboard) }
            }
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

    fun loadObstacleFromFile(file: File, position: BlockVector3, world: World, force: Boolean): Clipboard {
        val clipboard: Clipboard
        BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getReader(file.inputStream()).use { reader ->
            clipboard = reader.read()
        }

        val pivot = getPivotLine(clipboard, BlockTypes.DIAMOND_BLOCK!!)
        if(pivot == null) {
            if(force) ParkourPlayground.pluginLogger.warning("Loading obstacle from path ${file.path.toString()} without a valid start")
            else throw IllegalStateException("Cannot load obstacle without a start pivot of 5 diamond blocks")
        } else {
            clipboard.origin = pivot
        }

        if(getPivotLine(clipboard, BlockTypes.REDSTONE_BLOCK!!) == null) {
            if(force) ParkourPlayground.pluginLogger.warning("Loading obstacle from path ${file.path.toString()} without a valid end")
            else throw IllegalStateException("Cannot load obstacle without an end pivot of 5 redstone blocks")
        }

        val editSession = WorldEdit.getInstance()
            .newEditSession(BukkitAdapter.adapt(world))

        val operation = ClipboardHolder(clipboard)
            .createPaste(editSession)
            .to(position)
            .ignoreAirBlocks(false)
            .build()

        Operations.complete(operation)
        editSession.close()

        return clipboard
    }

    data class LoadableObstacle(val schematic: File)
    data class LoadedObstacle(val schematic: File, val type: ObstacleType, val startPos: BlockVector3, val endPos: BlockVector3)

    enum class ObstacleType {
        NORMAL,
        TRIDENT,
        ELYTRA,
        WIND_CHARGE,
        BOAT
    }
}