package skywolf46.devain.controller.modules

import org.koin.core.component.inject
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import skywolf46.devain.*
import skywolf46.devain.controller.api.certainly
import skywolf46.devain.controller.api.requests.devain.DevAinPersistenceCountAPICall
import skywolf46.devain.controller.api.requests.openai.DallEAPICall
import skywolf46.devain.controller.api.requests.openai.GPTCompletionAPICall
import skywolf46.devain.controller.commands.discord.openai.DallEGenerationCommand
import skywolf46.devain.controller.commands.discord.openai.ModalGPTCommand
import skywolf46.devain.controller.commands.discord.openai.SimpleGPTCommand
import skywolf46.devain.model.rest.devain.data.request.GetRequest
import skywolf46.devain.platform.discord.DiscordBot
import skywolf46.devain.platform.plugin.PluginModule
import skywolf46.devain.util.TimeUtil

class OpenAIModule(private val apiKey: String) : PluginModule("OpenAI Integration") {
    private val module = module {
        single { GPTCompletionAPICall(apiKey) }
        single { DallEAPICall(apiKey) }
    }

    private val apiCall by inject<DevAinPersistenceCountAPICall>()

    private val discordBot by inject<DiscordBot>()

    override fun onInitialize() {
        loadKoinModules(module)
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
            DallEGenerationCommand()
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
                )
            ),
            "DallE" to listOf(
                PluginStatistics("처리 완료 개수", "%,d".format(apiCall.certainly(GetRequest(KEY_DALLE_PROCEED_COUNT)).value)),
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
}