package xyz.devcmb.playground

import org.bukkit.Bukkit
import org.bukkit.plugin.PluginManager
import xyz.devcmb.playground.controllers.*

object ControllerDelegate {
    private val controllers: HashMap<String, IController> = HashMap()

    fun registerAllControllers() {
        if(Constants.IS_DEVELOPMENT) {
            registerController("debugController", DebugController())
        }
    }

    fun registerController(id: String, controller: IController) {
        val manager: PluginManager = Bukkit.getServer().pluginManager
        manager.registerEvents(controller, ParkourPlayground.plugin)

        controllers[id] = controller
        controller.init() // guess who forgot this :eyes:
        ParkourPlayground.pluginLogger.info("Controller $id registered sucessfully")
    }

    fun getController(id: String): IController? {
        val controller: IController? = controllers[id]
        if(controller == null) {
            ParkourPlayground.pluginLogger.warning("Controller with id $id not found")
            return null
        }

        return controller
    }

    fun cleanupControllers() {
        controllers.forEach {
            it.value.cleanup()
        }
    }
}