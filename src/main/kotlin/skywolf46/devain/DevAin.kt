package skywolf46.devain

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import org.json.simple.parser.JSONParser
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.dsl.module
import skywolf46.devain.config.BotConfig
import skywolf46.devain.controller.api.requests.deepl.DeepLTranslationAPICall
import skywolf46.devain.controller.api.requests.devain.DevAinAppPropertiesAPICall
import skywolf46.devain.controller.api.requests.devain.DevAinPersistenceCountAPICall
import skywolf46.devain.controller.api.requests.devain.DevAinUpdatePersistenceCountAPICall
import skywolf46.devain.controller.api.requests.openai.GPTCompletionAPICall
import skywolf46.devain.controller.commands.discord.deepl.DeepLKoreanTranslationCommand
import skywolf46.devain.controller.commands.discord.deepl.DeepLSimpleTranslationCommand
import skywolf46.devain.controller.commands.discord.deepl.DeepLTranslationCommand
import skywolf46.devain.controller.commands.discord.devain.DevAinStatusCommand
import skywolf46.devain.controller.commands.discord.gpt.ModalGPTCommand
import skywolf46.devain.controller.commands.discord.gpt.SimpleGPTCommand
import skywolf46.devain.model.store.SqliteStore
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
        single { SqliteStore() }

        // API Call Initialization
        single { GPTCompletionAPICall(botConfig.openAIToken) }
        single { DeepLTranslationAPICall(botConfig.deepLToken) }
        single { DevAinAppPropertiesAPICall() }
        single { DevAinPersistenceCountAPICall() }
        single { DevAinUpdatePersistenceCountAPICall() }
    }

    lateinit var discordBot: DiscordBot
        private set



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

            ModalGPTCommand(
                "modal-gpt-fast",
                "모달을 사용해 GPT-4에게 질문합니다. GPT-0613은 빠르지만, GPT-4보다 덜 정확할 수 있습니다.",
                "gpt-4-0613"
            ),


            ModalGPTCommand(
                "modal-gpt-more",
                "모달을 사용해 GPt-4에게 질문합니다.  GPT-4는 느리지만, 조금 더 논리적인 답변을 기대할 수 있습니다.",
                "gpt-4"
            ),


            ModalGPTCommand(
                "modal-gpt",
                "모달을 사용해 GPT-3.5에게 질문합니다. GPT-3.5는 빠르지만, GPT-4보다 부정확합니다.",
                "gpt-3.5-turbo"
            ),

            DeepLTranslationCommand(),
            DeepLSimpleTranslationCommand(),
            DeepLKoreanTranslationCommand(),

            DevAinStatusCommand()
        )

    }

}