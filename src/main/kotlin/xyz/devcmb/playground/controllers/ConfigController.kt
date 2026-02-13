package xyz.devcmb.playground.controllers

import org.bukkit.configuration.MemorySection
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import xyz.devcmb.playground.ControllerDelegate.registerController
import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.annotations.Configurable
import xyz.devcmb.playground.annotations.Controller

@Controller("configController", priority = Controller.Priority.HIGHEST)
class ConfigController : IController {
    override fun init() {
        val reflections = Reflections(
            ParkourPlayground::class.java.packageName,
            Scanners.FieldsAnnotated
        )

        reflections.getFieldsAnnotatedWith(Configurable::class.java).forEach { field ->
            field.isAccessible = true

            val annotation: Configurable = field.getAnnotation(Configurable::class.java)
            val config = ParkourPlayground.plugin.config

            val path = annotation.path
            val value = if(!config.contains(path)) null else when(field.type) {
                Int::class.java,
                Int::class.javaPrimitiveType -> config.getInt(path)

                Long::class.javaPrimitiveType,
                Long::class.java -> config.getLong(path)

                Double::class.javaPrimitiveType,
                Double::class.java -> config.getDouble(path)

                Float::class.javaPrimitiveType,
                Float::class.java -> config.getDouble(path).toFloat()

                String::class.java -> config.getString(path)

                MutableMap::class.java,
                HashMap::class.java -> config.getConfigurationSection(path)
                    ?.getKeys(false)
                    ?.associateWith { key ->
                        config.get("$path.$key")
                    }
                    ?.toMap(HashMap())
                    ?: HashMap<String, MemorySection>()


                else -> config.get(path)
            }

            if(value != null) {
                ParkourPlayground.pluginLogger.info("Updated field ${field.name} to $value from config")
                field.set(null, value)
            }
        }
    }
}