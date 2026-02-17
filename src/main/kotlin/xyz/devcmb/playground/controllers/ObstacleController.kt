package xyz.devcmb.playground.controllers

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.session.ClipboardHolder
import org.bukkit.Location
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

        try {
            FileOutputStream(File(saveDirectory, name)).use { outputStream ->
                BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC
                    .getWriter(outputStream)
                    .use { writer -> writer.write(clipboard) }
            }
            onSuccess()
        } catch(e: Exception) {
            onError(e.message ?: "Unknown error")
        }
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