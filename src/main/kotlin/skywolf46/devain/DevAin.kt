package skywolf46.devain

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import org.json.simple.parser.JSONParser
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.dsl.module
import skywolf46.devain.controller.commands.discord.gpt.SimpleGPTCommand
import skywolf46.devain.config.BotConfig
import skywolf46.devain.controller.api.requests.deepl.DeepLTranslationAPICall
import skywolf46.devain.controller.api.requests.openai.GPTCompletionAPICall
import skywolf46.devain.controller.commands.discord.deepl.DeepLTranslationCommand
import skywolf46.devain.platform.discord.DiscordBot


fun main(args: Array<String>) {
    DevAin().init()
}

class DevAin : KoinComponent {
    val version = "1.2.0 - Grenade Muffin"

    private val botConfig = BotConfig()

    private val devAinModule = module {
        single {
            HttpClient(CIO) {
                engine {
                    requestTimeout = 0
                }
            }
        }
        single { this@DevAin }
        single { BotConfig() }
        single { JSONParser() }
        single { GPTCompletionAPICall(botConfig.openAIToken) }
        single { DeepLTranslationAPICall(botConfig.deepLToken) }
    }

    lateinit var discordBot: DiscordBot
        private set


    val startTime = System.currentTimeMillis()


    internal fun init() {
        println("DevAin $version - 초기화 시작")
        startKoin {
            modules(devAinModule)
            initializeBot()
        }
    }

    private fun initializeBot() {
        println("디스코드 봇 활성화중..")
        discordBot = DiscordBot(this, botConfig)
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

            SimpleGPTCommand(
                "ask-fast",
                "GPT-4-0613에게 질문합니다. GPT-4는 느리지만, 조금 더 논리적인 답변을 기대할 수 있습니다.",
                "gpt-4-0613"
            ),

            DeepLTranslationCommand()
        )

    }

}