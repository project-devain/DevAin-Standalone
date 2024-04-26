package skywolf46.devain.controller.modules

import org.koin.core.component.inject
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import skywolf46.devain.*
import skywolf46.devain.annotations.config.ConfigDefault
import skywolf46.devain.annotations.config.MarkConfigElement
import skywolf46.devain.apicall.certainly
import skywolf46.devain.apicall.networking.GetRequest
import skywolf46.devain.controller.api.requests.arxiv.ArxivSearchAPICall
import skywolf46.devain.controller.api.requests.devain.DevAinPersistenceCountAPICall
import skywolf46.devain.controller.api.requests.eve.EvEOnlineStatusAPICall
import skywolf46.devain.controller.api.requests.google.GoogleSearchAPICall
import skywolf46.devain.controller.api.requests.openai.DallEAPICall
import skywolf46.devain.controller.api.requests.openai.GPTCompletionAPICall
import skywolf46.devain.controller.api.requests.openweather.OpenWeatherAPICall
import skywolf46.devain.controller.api.requests.openweather.OpenWeatherForecastAPICall
import skywolf46.devain.controller.commands.discord.openai.*
import skywolf46.devain.model.api.openai.completion.functions.*
import skywolf46.devain.model.data.config.ConfigElement
import skywolf46.devain.model.data.config.fetchSharedDocument
import skywolf46.devain.model.data.store.OpenAIFunctionStore
import skywolf46.devain.platform.discord.DiscordBot
import skywolf46.devain.platform.plugin.PluginModule
import skywolf46.devain.util.TimeUtil

class OpenAIModule : PluginModule("OpenAI Integration") {

    private val apiCall by inject<DevAinPersistenceCountAPICall>()

    private val storage by inject<OpenAIFunctionStore>()

    private val discordBot by inject<DiscordBot>()

    private val functions = mutableListOf(
        DelayDeclaration(),
        EvEOnlineStatusDeclaration(),
        GoogleSearchDeclaration(),
        OpenWeatherCallDeclaration(),
        OpenWeatherForecastDeclaration(),
        ArxivSearchDeclaration(),
        TimeDeclaration()
    )

    override fun onInitialize() {
        document.fetchSharedDocument<OpenAIConfigElement>(pluginName) { config ->
            loadKoinModules(module {
                single { GPTCompletionAPICall(config.openAIKey) }
                single { DallEAPICall(config.openAIKey) }
                single { OpenWeatherAPICall(config.openWeatherKey) }
                single { OpenWeatherForecastAPICall(config.openWeatherKey) }
                single { OpenAIFunctionStore() }
                single { EvEOnlineStatusAPICall() }
                single { GoogleSearchAPICall(config.googleApiKey, config.googleSearchEngineId) }
                single { ArxivSearchAPICall() }
            })

            initFunctions()
            registerCommands()
        }
    }

    private fun initFunctions() {
        functions.forEach { function ->
            storage.registerFunction(function)
        }
    }

    private fun registerCommands() {
        registerDefaultCommands()
        registerModalCommands()
        registerTestFeatureCommands()
    }

    private fun registerDefaultCommands() {
        discordBot.registerCommands(
            SimpleGPTCommand(
                "ask",
                "GPT-3.5에게 질문합니다. GPT-4보다 비교적 빠릅니다. 세션을 보관하지 않으며, 명령어당 하나의 세션으로 인식합니다.",
                "gpt-3.5-turbo-16k"
            ),
            SimpleGPTCommand(
                "ask-more",
                "GPT-4에게 질문합니다. GPT-4는 느리지만, 조금 더 논리적인 답변을 기대할 수 있습니다.",
                "gpt-4"
            ),

//            SimpleGPTCommand(
//                "ask-fast",
//                "GPT-4-0613에게 질문합니다. GPT-4는 느리지만, 조금 더 논리적인 답변을 기대할 수 있습니다.",
//                "gpt-4-0613"
//            ),
            SimpleGPTCommand(
                "ask-fast",
                "GPT-4-0125에게 질문합니다. GPT-4는 느리지만, 조금 더 논리적인 답변을 기대할 수 있습니다.",
                "gpt-4-0125-preview"
            ),

            SimpleGPTCommand(
                "ask-fast-turbo",
                "GPT-4-1106-preview(Turbo)에게 질문합니다. GPT-4는 느리지만, 조금 더 논리적인 답변을 기대할 수 있습니다.",
                "gpt-4-1106-preview"
            ),


            ImageGPTCommand(
                "ask-vision",
                "GPT-4-vision-preview에게 질문합니다. GPT-4는 느리지만, 조금 더 논리적인 답변을 기대할 수 있습니다.",
                "gpt-4-vision-preview"
            ),

            ImageGPTCommand(
                "ask-vision-exp",
                "GPT-4-1106-vision-preview에게 질문합니다. GPT-4는 느리지만, 조금 더 논리적인 답변을 기대할 수 있습니다.",
                "gpt-4-1106-vision-preview"
            ),


            )
    }

    private fun registerModalCommands() {
        discordBot.registerCommands(
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
                "gpt-3.5-turbo-16k"
            )
        )
    }

    private fun registerTestFeatureCommands() {
        discordBot.registerCommands(
            TestGPTCommand("test-gpt", "펑션을 사용하는 실험적인 GPT 명령입니다.", "gpt-3.5-turbo-16k"),
            ArxivGPTCommand("arxiv-gpt", "ArXiv 검색을 사용하는 실험적인 GPT 명령입니다.", "gpt-4-turbo"),
            DallEGenerationCommand(),
        )
    }

    override suspend fun getStatistics(): Map<String, List<PluginStatistics>> {
        return mapOf(
            "ChatGPT" to listOf(
                PluginStatistics("처리 완료 개수", "%,d".format(apiCall.certainly(GetRequest(KEY_GPT_PROCEED_COUNT)).value)),
                PluginStatistics("처리 완료 토큰", "%,d".format(apiCall.certainly(GetRequest(KEY_GPT_PROCEED_TOKEN)).value)),
                PluginStatistics(
                    "요청 처리 시간",
                    TimeUtil.toTimeString(apiCall.certainly(GetRequest(KEY_GPT_PROCEED_TIME)).value)
                ),
                PluginStatistics(
                    "펑션 처리 완료 개수",
                    "%,d".format(apiCall.certainly(GetRequest(KEY_GPT_FUNCTION_PROCEED_COUNT)).value)
                ),
                PluginStatistics(
                    "펑션 요청 처리 시간", TimeUtil.toTimeString(
                        apiCall.certainly(
                            GetRequest(
                                KEY_GPT_FUNCTION_PROCEED_TIME
                            )
                        ).value
                    )
                ),
            ),
            "DallE" to listOf(
                PluginStatistics(
                    "처리 완료 개수",
                    "%,d".format(apiCall.certainly(GetRequest(KEY_DALLE_PROCEED_COUNT)).value)
                ),
                PluginStatistics(
                    "요청 처리 시간",
                    TimeUtil.toTimeString(apiCall.certainly(GetRequest(KEY_DALLE_PROCEED_TIME)).value)
                )
            )
        )
    }

    override fun getVersion(): String {
        return "built-in"
    }

    data class OpenAIConfigElement(
        @ConfigDefault.String("YOUR-API-TOKEN-HERE")
        @MarkConfigElement("OpenAI API Key")
        val openAIKey: String,
        @ConfigDefault.String("YOUR-API-TOKEN-HERE")
        @MarkConfigElement("OpenWeather API Key")
        val openWeatherKey: String,
        @ConfigDefault.String("YOUR-API-TOKEN-HERE")
        @MarkConfigElement("Google API Key")
        val googleApiKey: String,
        @ConfigDefault.String("YOUR-API-TOKEN-HERE")
        @MarkConfigElement("Google Search Engine ID")
        val googleSearchEngineId: String,
    ) : ConfigElement
}