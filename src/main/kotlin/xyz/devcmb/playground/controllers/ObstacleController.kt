package xyz.devcmb.playground.controllers

import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
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

    enum class ObstacleType {
        NORMAL,
        TRIDENT,
        ELYTRA,
        WIND_CHARGE,
        BOAT
    }
}