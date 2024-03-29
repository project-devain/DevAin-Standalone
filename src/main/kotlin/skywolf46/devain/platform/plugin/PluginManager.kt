package skywolf46.devain.platform.plugin

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class PluginManager {
    private val plugins = mutableListOf<PluginModule>()

    private val enabledPlugins = mutableListOf<PluginModule>()

    private val isInitialized = AtomicBoolean(false)

    private val lock = ReentrantLock()


    fun init() {
        lock.withLock {
            if (isInitialized.get()) {
                throw IllegalStateException("PluginManager is already initialized")
            }
            enabledPlugins.addAll(plugins.filter {
                runCatching {
                    it.canBeLoaded()
                }.getOrElse { false }
            })
            isInitialized.set(true)
            iterateEnabledPlugin {
                it.onPreInitialize()
            }
            iterateEnabledPlugin {
                it.onInitialize()
            }
            iterateEnabledPlugin {
                it.onPostInitialize()
            }
        }
    }

    private fun iterateEnabledPlugin(executor: (PluginModule) -> Unit) {
        enabledPlugins.toList().forEach { plugin ->
            runCatching {
                executor(plugin)
            }.onFailure {
                RuntimeException("Failed to initialize plugin ${plugin.pluginName}", it).printStackTrace()
                enabledPlugins.remove(plugin)
            }
        }
    }

    fun addPlugin(pluginModule: PluginModule) {
        lock.withLock {
            if (isInitialized.get()) {
                throw IllegalStateException("PluginManager is already initialized")
            }
            if (getPluginByName(pluginModule.pluginName) != null) {
                throw IllegalStateException("Same plugin name is already registered")
            }
            plugins.add(pluginModule)
        }
    }

    fun addPlugins(vararg pluginModule: PluginModule) {
        pluginModule.forEach {
            addPlugin(it)
        }
    }

    fun getPlugins() = plugins.toList()

    fun getEnabledPlugins() = enabledPlugins.toList()

    fun getPluginByName(name: String) = plugins.find { it.pluginName == name }

    fun isEnabled(pluginName: String) = enabledPlugins.any { it.pluginName == pluginName }

    fun isEnabled(pluginModule: PluginModule) = enabledPlugins.contains(pluginModule)

}