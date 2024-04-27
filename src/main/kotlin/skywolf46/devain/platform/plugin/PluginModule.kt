package skywolf46.devain.platform.plugin

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import skywolf46.devain.configurator.ConfigDocumentRoot
import java.io.File

abstract class PluginModule(val pluginName: String) : KoinComponent {

    lateinit var dataDirectory: File
        internal set

    val document by inject<ConfigDocumentRoot>()


    open fun canBeLoaded(): Boolean {
        return true
    }

    open fun onPreInitialize() {
        // Do nothing
    }

    open fun onInitialize() {
        // Do nothing
    }

    open fun onPostInitialize() {
        // Do nothing
    }

    open fun onInitializeComplete() {
        // Do nothing
    }

    open suspend fun getStatistics(): Map<String, List<PluginStatistics>> {
        return emptyMap()
    }

    open fun getVersion(): String {
        return "unspecified"
    }

    data class PluginStatistics(val name: String, val value: String)
}