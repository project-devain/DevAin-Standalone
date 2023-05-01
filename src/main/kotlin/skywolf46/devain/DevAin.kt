package skywolf46.devain

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.dsl.module
import skywolf46.devain.commands.discord.devain.DevAinStatusCommand
import skywolf46.devain.commands.discord.gpt.GPTTokenCalculationCommand
import skywolf46.devain.commands.discord.gpt.SimpleGPTCommand
import skywolf46.devain.commands.discord.gpt.sessions.SessionGPTCommand
import skywolf46.devain.commands.discord.gpt.sessions.SessionInfoCommand
import skywolf46.devain.commands.discord.gpt.sessions.StartGPTSessionCommand
import skywolf46.devain.commands.discord.presets.user.UserPresetAddCommand
import skywolf46.devain.commands.discord.presets.user.UserPresetListCommand
import skywolf46.devain.commands.discord.presets.user.UserPresetShareCommand
import skywolf46.devain.config.BotConfig
import skywolf46.devain.data.storage.ChattingSessionStorage
import skywolf46.devain.data.storage.PresetStorage
import skywolf46.devain.data.storage.SessionModelStorage
import skywolf46.devain.data.storage.SessionTokenStorage
import skywolf46.devain.platform.discord.DiscordBot


fun main(args: Array<String>) {
    DevAin().init()
}

class DevAin : KoinComponent {
    val version = "1.1.0 - Double Barrel Rollcake"

    private val devAinModule = module {
        single { BotConfig() }
        single { PresetStorage() }
        single { ChattingSessionStorage() }
        single { SessionTokenStorage() }
        single { SessionModelStorage() }
        single {
            HttpClient(CIO) {
                engine {
                    requestTimeout = 0
                }
            }
        }
        single { this@DevAin }
    }

    lateinit var discordBot: DiscordBot
        private set


    val startTime = System.currentTimeMillis()


    internal fun init() {
        println("DevAin $version - 초기화 시작")
        startKoin {
            modules(devAinModule)
            loadConfig()
            initializeBot()
        }
    }

    private fun loadConfig() {
        println("설정 불러오는 중..")
    }

    private fun initializeBot() {
        println("디스코드 봇 활성화중..")
        discordBot = DiscordBot(this, inject<BotConfig>().value)
        initializeCommands()
    }

    private fun initializeCommands() {
        println("명령어 초기화중..")
        discordBot.registerCommands(
            SimpleGPTCommand(
                "ask",
                "GPT-3.5에게 질문합니다. GPT-4보다 비교적 빠릅니다. 세션을 보관하지 않으며, 명령어당 하나의 세션으로 인식합니다.",
                "gpt-3.5-turbo"
            ),
            SimpleGPTCommand(
                "ask-more",
                "GPT-4에게 질문합니다. GPT-4는 느리지만, 조금 더 논리적인 답변을 기대할 수 있습니다.",
                "gpt-4"
            ),
            // GPT Session
            StartGPTSessionCommand(),
            SessionInfoCommand(),
            GPTTokenCalculationCommand(),
            SessionGPTCommand("session", "GPT 세션을 사용하여 질문합니다."),

            // User Presets
            UserPresetListCommand(),
            UserPresetAddCommand(),
            UserPresetShareCommand(),


            // DevAin Status
            DevAinStatusCommand()
        )

    }

}