package skywolf46.devain

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.dsl.module
import skywolf46.devain.config.BotConfig
import skywolf46.devain.controller.modules.*
import skywolf46.devain.model.data.config.ConfigDocumentRoot
import skywolf46.devain.platform.discord.DiscordBot
import skywolf46.devain.platform.plugin.PluginManager
import skywolf46.devain.platform.plugin.PluginModule
import java.io.File


fun main(args: Array<String>) {
    DevAin().init()
}

class DevAin : KoinComponent {
    val version = "1.3.0 - Radioactive Emmer Bread"

    internal fun init() {
        println("DevAin $version - 초기화 시작")
        startKoin {  }
        initializeBuiltInPlugins()
        while (true) {
            Thread.sleep(Long.MAX_VALUE)
        }
    }

    private fun initializeBuiltInPlugins() {
        println("플러그인 활성화중..")
        PluginManager().apply {
            addPlugins(
                DevAinModule(),
                DiscordModule(),
                OpenAIModule(),
                DeepLModule(),
                StabilityAIModule(),
                CohereModule(),
                AnthropicModule(),
                GroqModule(),
                KModule(),
                object : PluginModule("Test Fatal Plugin") {
                    override fun canBeLoaded(): Boolean {
                        return false
                    }
                }
            )
            init()
        }
    }

}