package xyz.devcmb.playground.controllers

import org.apache.commons.io.FileUtils
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.WorldSetupException
import xyz.devcmb.playground.annotations.Configurable
import xyz.devcmb.playground.annotations.Controller
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

@Controller("worldController", Controller.Priority.MEDIUM)
class WorldController : IController {
    companion object {
        @field:Configurable("lobby.world")
        var lobbyWorld: String = "hub"

        @field:Configurable("game.template_world_path")
        var templatePath: String = "templates/worlds/game"
    }

    var lobbyBukkitWorld: World? = null

    override fun init() {
        lobbyBukkitWorld = Bukkit.getWorld(lobbyWorld)
        if(lobbyBukkitWorld == null) {
            if(!Files.exists(Path.of(lobbyWorld))) {
                ParkourPlayground.pluginLogger.severe("Lobby world does not exist")
                return
            }

            lobbyBukkitWorld = Bukkit.createWorld(WorldCreator(lobbyWorld))
        }
    }

    override fun cleanup() {
        cleanupWorlds()
    }

    fun cleanupWorlds() {
        Bukkit.getWorldContainer().listFiles().forEach { file ->
            if(file.isDirectory && file.name.contains("temp_world_")) {
                if(Bukkit.getWorld(file.name) !== null) {
                    Bukkit.unloadWorld(file.name, false)
                }

                // my savior
                // https://www.spigotmc.org/threads/cant-delete-world-folder-after-unloading-it.314857/
                fun deleteDir(file2: File) {
                    val contents = file2.listFiles()
                    if (contents != null) {
                        for (f in contents) {
                            deleteDir(f)
                        }
                    }
                    file2.delete()
                }

                deleteDir(file)
            }
        }
    }

    fun setupGameWorld(): World {
        if (!Files.exists(Path.of(templatePath))) {
            throw WorldSetupException("Template path does not exist")
        }

        return createTemporaryWorldFromTemplate(File(templatePath))
    }

    // yes this is """inspired""" by mcc island
    private fun createTemporaryWorldFromTemplate(templateDir: File): World {
        val worldName = "temp_world_${UUID.randomUUID().toString().replace("-", "_")}"

        FileUtils.copyDirectory(
            templateDir,
            File(Bukkit.getWorldContainer(), worldName)
        )

        return Bukkit.createWorld(WorldCreator(worldName))!!
    }
}