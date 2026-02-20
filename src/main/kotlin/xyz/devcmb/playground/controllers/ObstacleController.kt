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
import org.bukkit.Location
import org.bukkit.Material
import xyz.devcmb.playground.annotations.Configurable
import xyz.devcmb.playground.annotations.Controller
import java.io.File
import java.io.FileOutputStream
import kotlin.io.path.Path

@Controller("obstacleController", Controller.Priority.HIGH)
class ObstacleController : IController {
    companion object {
        @field:Configurable("templates.root_path")
        var templateRootPath: String = "templates"

        @field:Configurable("templates.obstacles_path")
        var obstaclesPath: String = "obstacles"
    }

    override fun init() {
    }

    fun saveObstacle(clipboard: Clipboard, name: String, type: ObstacleType, onSuccess: () -> Unit, onError: (err: String) -> Unit) {
        val saveDirectory = File(Path(templateRootPath, obstaclesPath, type.name.lowercase()).toString())
        if(!saveDirectory.exists()) {
            saveDirectory.mkdirs()
        }

        if(!checkForPositions(BlockTypes.DIAMOND_BLOCK!!, clipboard)) {
            onError("A row of 5 diamond blocks indicating the start of the segment was not found in the schematic!")
            return
        }

        if(!checkForPositions(BlockTypes.REDSTONE_BLOCK!!, clipboard)) {
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
    private fun checkForPositions(type: BlockType, clipboard: Clipboard): Boolean {
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

            if (check(1, 0) || check(0, 1)) {
                return true
            }
        }

        return false
    }

    fun loadObstacle(file: File, position: Location) {
        val clipboard: Clipboard
        BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getReader(file.inputStream()).use { reader ->
            clipboard = reader.read()
        }

        val editSession = WorldEdit.getInstance()
            .newEditSession(BukkitAdapter.adapt(position.world))

        val operation = ClipboardHolder(clipboard)
            .createPaste(editSession)
            .to(BlockVector3.at(position.x, position.y, position.z))
            .ignoreAirBlocks(false)
            .build()

        Operations.complete(operation)
        editSession.close()
    }

    data class LoadableObstacle(val schematic: File)

    enum class ObstacleType {
        NORMAL,
        TRIDENT,
        ELYTRA,
        WIND_CHARGE,
        BOAT
    }
}