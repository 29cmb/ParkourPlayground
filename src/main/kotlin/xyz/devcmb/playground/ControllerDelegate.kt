package xyz.devcmb.playground

import org.bukkit.Bukkit
import org.bukkit.plugin.PluginManager
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import xyz.devcmb.playground.annotations.Controller
import xyz.devcmb.playground.controllers.*

object ControllerDelegate {
    private val controllers: HashMap<String, IController> = HashMap()

    @Suppress("UNCHECKED_CAST")
    fun registerAllControllers() {
        val reflections = Reflections(
            ParkourPlayground::class.java.packageName,
            Scanners.TypesAnnotated
        )

        reflections.getTypesAnnotatedWith(Controller::class.java)
            .filter { IController::class.java.isAssignableFrom(it) }
            .forEach { clazz ->
                val annotation = clazz.getAnnotation(Controller::class.java)
                val controllerClass = clazz as Class<out IController>

                val instance: IController =
                    controllerClass.getDeclaredConstructor().newInstance()

                registerController(annotation.id, instance)
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