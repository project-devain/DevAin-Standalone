package skywolf46.devain.controller.modules

import org.koin.core.component.inject
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import skywolf46.devain.KEY_GROQ_GENERATION_PROCEED_COUNT
import skywolf46.devain.apicall.certainly
import skywolf46.devain.apicall.networking.GetRequest
import skywolf46.devain.configurator.APITokenElement
import skywolf46.devain.configurator.fetchSharedDocument
import skywolf46.devain.controller.api.requests.devain.DevAinPersistenceCountAPICall
import skywolf46.devain.controller.api.requests.groq.GroqAPICall
import skywolf46.devain.controller.commands.discord.groq.SimpleGroqCommand
import skywolf46.devain.platform.discord.DiscordBot
import skywolf46.devain.platform.plugin.PluginModule

class GroqModule : PluginModule("Groq Integration") {
    private val discord by inject<DiscordBot>()

    private val apiCall by inject<DevAinPersistenceCountAPICall>()


    override fun onInitialize() {
        document.fetchSharedDocument<APITokenElement>(pluginName) { config ->
            loadKoinModules(module {
                single { GroqAPICall(config.apiToken) }
            })
            discord.registerCommands(
                SimpleGroqCommand("xllama", "LLAAAAAAAAAAMAAAAAAAAA", "llama3-70b-8192", "Llama3 70B"),
                SimpleGroqCommand("llama", "LLAAAAAAAAAAMAAAAAAAAA", "llama3-8b-8192", "Llama3 8B"),
                SimpleGroqCommand("mixtral", "Mixtral (Groq)", "mixtral-8x7b-32768", "Mixtral 8 x 7B"),
                SimpleGroqCommand("gemma", "Gemma (Groq)", "gemma-7b-it", "Gemma"),
            )
        }
    }

    override suspend fun getStatistics(): Map<String, List<PluginStatistics>> {
        return mapOf(
            "Text Generation" to listOf(
                PluginStatistics(
                    "총 생성 횟수",
                    "%,d".format(apiCall.certainly(GetRequest(KEY_GROQ_GENERATION_PROCEED_COUNT)).value)
                ),
            )
        )
    }

    override fun getVersion(): String {
        return "Alpha"
    }
}