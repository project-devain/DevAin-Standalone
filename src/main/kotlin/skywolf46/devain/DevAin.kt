package skywolf46.devain

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.dsl.module
import skywolf46.devain.config.BotConfig
import skywolf46.devain.controller.modules.*
import skywolf46.devain.platform.discord.DiscordBot
import skywolf46.devain.platform.plugin.PluginManager
import skywolf46.devain.platform.plugin.PluginModule


fun main(args: Array<String>) {
    DevAin().init()
}

class DevAin : KoinComponent {
    val version = "1.2.0 - Grenade Muffin"

    private val botConfig = BotConfig()

    lateinit var discordBot: DiscordBot
        private set

    internal fun init() {
        println("DevAin $version - 초기화 시작")
        initializeBot()
        startKoin {
            loadKoinModules(module {
                single { discordBot }
                single { botConfig }

                single { PluginManager() }
            })
        }
        initializeBuiltInPlugins()
        finalizeBot()
    }

    private fun initializeBot() {
        println("디스코드 봇 활성화중..")
        discordBot = DiscordBot(this, botConfig)
    }

    private fun initializeBuiltInPlugins() {
        println("플러그인 활성화중..")
        get<PluginManager>().apply {
            addPlugins(
                DevAinModule(),
                OpenAIModule(botConfig, botConfig.openAIToken),
                DeepLModule(botConfig.deepLToken),
                StabilityAIModule(botConfig.dreamStudioToken),
                CohereModule(botConfig.cohereToken),
                object : PluginModule("Test Fatal Plugin") {
                    override fun canBeLoaded(): Boolean {
                        return false
                    }
                }
            )
            init()
        }
    }

    private fun finalizeBot() {
        println("디스코드 봇 설정 마무리중..")
        discordBot.finishSetup()
    }


}