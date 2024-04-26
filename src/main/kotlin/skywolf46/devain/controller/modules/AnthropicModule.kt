package skywolf46.devain.controller.modules

import org.koin.core.component.inject
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import skywolf46.devain.controller.api.requests.anthropic.ClaudeAPICall
import skywolf46.devain.controller.api.requests.devain.DevAinPersistenceCountAPICall
import skywolf46.devain.controller.commands.discord.anthropic.ClaudeCommand
import skywolf46.devain.model.data.config.APITokenElement
import skywolf46.devain.model.data.config.fetchSharedDocument
import skywolf46.devain.platform.discord.DiscordBot
import skywolf46.devain.platform.plugin.PluginModule

class AnthropicModule : PluginModule("Anthropic Integration") {

    private val discordBot by inject<DiscordBot>()

    override fun onInitialize() {
        document.fetchSharedDocument<APITokenElement>("Anthropic Integration") { config ->
            loadKoinModules(module {
                single { ClaudeAPICall(config.apiToken) }
            })
            discordBot.registerCommands(
                ClaudeCommand()
            )
        }
    }
}