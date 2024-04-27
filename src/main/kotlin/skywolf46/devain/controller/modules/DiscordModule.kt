package skywolf46.devain.controller.modules

import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import skywolf46.devain.configurator.APITokenElement
import skywolf46.devain.configurator.fetchSharedDocument
import skywolf46.devain.platform.discord.DiscordBot
import skywolf46.devain.platform.plugin.PluginModule

class DiscordModule : PluginModule("Discord Integration") {
    private val discordBot = DiscordBot()

    override fun onPreInitialize() {
        loadKoinModules(module {
            single { discordBot }
        })
    }

    override fun onPostInitialize() {
        document.fetchSharedDocument<APITokenElement>("Discord") { config ->
            discordBot.finishSetup(config.apiToken)
        }
    }

    override fun getVersion(): String {
        return "built-in"
    }
}