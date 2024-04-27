package skywolf46.devain.controller.modules

import org.koin.core.component.inject
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import skywolf46.devain.KEY_COHERE_GENERATION_PROCEED_COUNT
import skywolf46.devain.apicall.certainly
import skywolf46.devain.apicall.networking.GetRequest
import skywolf46.devain.configurator.APITokenElement
import skywolf46.devain.configurator.fetchSharedDocument
import skywolf46.devain.controller.api.requests.cohere.CohereGenerationAPICall
import skywolf46.devain.controller.api.requests.cohere.CommandRPlusAPICall
import skywolf46.devain.controller.api.requests.devain.DevAinPersistenceCountAPICall
import skywolf46.devain.controller.commands.discord.cohere.CohereCommand
import skywolf46.devain.controller.commands.discord.cohere.CommandRCommand
import skywolf46.devain.controller.commands.discord.cohere.CommandRPlusCommand
import skywolf46.devain.platform.discord.DiscordBot
import skywolf46.devain.platform.plugin.PluginModule

class CohereModule : PluginModule("Cohere Integration") {
    private val discord by inject<DiscordBot>()

    private val apiCall by inject<DevAinPersistenceCountAPICall>()

    override fun onInitialize() {
        document.fetchSharedDocument<APITokenElement>(pluginName) { config ->
            loadKoinModules(module {
                single { CohereGenerationAPICall(config.apiToken) }
                single { CommandRPlusAPICall(config.apiToken) }
            })
            discord.registerCommands(
                CohereCommand(),
                CommandRCommand(),
                CommandRPlusCommand()
            )
        }
    }

    override suspend fun getStatistics(): Map<String, List<PluginStatistics>> {
        return mapOf(
            "Text Generation" to listOf(
                PluginStatistics(
                    "총 생성 횟수",
                    "%,d".format(apiCall.certainly(GetRequest(KEY_COHERE_GENERATION_PROCEED_COUNT)).value)
                ),
            )
        )
    }

    override fun getVersion(): String {
        return "Alpha"
    }
}