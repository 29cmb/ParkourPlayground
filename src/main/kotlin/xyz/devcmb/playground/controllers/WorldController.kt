package xyz.devcmb.playground.controllers

import org.apache.commons.io.FileUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator
import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.WorldSetupException
import xyz.devcmb.playground.annotations.Configurable
import xyz.devcmb.playground.annotations.Controller
import java.io.File
import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

@Controller("worldController", Controller.Priority.MEDIUM)
class WorldController : IController {
    companion object {
        @field:Configurable("lobby.world")
        var lobbyWorld: String = "hub"

        @field:Configurable("templates.root_path")
        var templateRootPath: String = "templates"

        @field:Configurable("templates.worlds_path")
        var templatesWorldsPath: String = "worlds"

        @field:Configurable("templates.game_template_world")
        var gameTemplateWorld: String = "game"
    }

    var lobbyBukkitWorld: World? = null

    override fun init() {
        // If the server crashes and doesn't save, it doesn't clear worlds
        // This is the workaround, do both on open and cleanup
        cleanupWorlds()

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
        val gamePath = Path.of(templateRootPath, templatesWorldsPath, gameTemplateWorld)
        if (!Files.exists(gamePath)) {
            throw WorldSetupException("Template path does not exist")
        }

        return createTemporaryWorldFromTemplate(File(gamePath.toString()))
    }

    // yes this is """inspired""" by mcc island
    fun createTemporaryWorldFromTemplate(templateDir: File): World {
        val worldName = "temp_world_${UUID.randomUUID().toString().replace("-", "_")}"
        val destination = File(Bukkit.getWorldContainer(), worldName)

        FileUtils.copyDirectory(
            templateDir,
            destination
        )

        Files.write(
            File(destination, "template.txt").toPath(),
            listOf(templateDir.name),
            StandardCharsets.UTF_8
        )

        val idFile = File(destination, "uid.dat")
        if(idFile.exists()) {
            idFile.delete()
        }

        return Bukkit.createWorld(WorldCreator(worldName))!!
    }

    fun saveWorldToTemplate(world: World) = saveWorldToTemplate(world, null)

    fun saveWorldToTemplate(world: World, templateName: String?) {
        var templateWorldName = templateName

        val worldFolder = File(Bukkit.getWorldContainer(), world.name)
        if(!worldFolder.exists()) {
            throw WorldSetupException("World directory does not exist")
        }

        val config = ParkourPlayground.plugin.config
        val position = config.getList("lobby.position")!!

        world.players.forEach {
            it.teleport(Location(
                lobbyBukkitWorld!!,
                position.get(0) as Double,
                position.get(1) as Double,
                position.get(2) as Double
            ))
        }

        Bukkit.unloadWorld(world, true)

        val templatesWorldFolder = File(templateRootPath, templatesWorldsPath)
        if(!templatesWorldFolder.exists()) {
            throw WorldSetupException("Templates world path doesn't exist")
        }

        if(templateWorldName == null) {
            val templateFile = File(worldFolder, "template.txt")
            if(!templateFile.exists()) {
                throw WorldSetupException("Template identification file does not exist")
            }

            templateWorldName = templateFile.readText().replace("\n", "").replace("\r", "");
        }

        Bukkit.getScheduler().runTaskLater(ParkourPlayground.plugin, Runnable {
            val templateWorldDir = File(
                templatesWorldFolder.toPath().toString(),
                templateWorldName
            )
            FileUtils.copyDirectory(worldFolder, templateWorldDir)

            val idFile = File(templateWorldDir, "uid.dat")
            if(idFile.exists()) {
                idFile.delete()
            }
        }, 60L)
    }

    fun getTemplateWorlds(): ArrayList<File> {
        val worlds: ArrayList<File> = ArrayList()

        val worldsFolder = Path.of(templateRootPath, templatesWorldsPath)
        if(!Files.exists(worldsFolder)) {
            throw WorldSetupException("Templates world path doesn't exist")
        }

        File(worldsFolder.toString()).listFiles().forEach { file ->
            if(file.isDirectory) {
                worlds.add(file)
            }
        }

        return worlds
    }

    data class TemplateWorld(val folder: File)
}