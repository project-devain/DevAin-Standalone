package skywolf46.devain.controller.modules

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import org.json.simple.parser.JSONParser
import org.koin.core.component.inject
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import skywolf46.devain.config.BotConfig
import skywolf46.devain.controller.api.requests.devain.DevAinAppPropertiesAPICall
import skywolf46.devain.controller.api.requests.devain.DevAinPersistenceCountAPICall
import skywolf46.devain.controller.api.requests.devain.DevAinUpdatePersistenceCountAPICall
import skywolf46.devain.controller.commands.discord.devain.DevAinStatusCommand
import skywolf46.devain.model.store.SqliteStore
import skywolf46.devain.platform.discord.DiscordBot
import skywolf46.devain.platform.plugin.PluginModule

class DevAinModule : PluginModule("DevAin Core") {
    private val devAinModule = module {
        single {
            HttpClient(CIO) {
                engine {
                    requestTimeout = 0
                }
            }
        }
        single { BotConfig() }
        single { JSONParser() }
        single { SqliteStore() }
        // API Call Initialization
        single { DevAinAppPropertiesAPICall() }
        single { DevAinPersistenceCountAPICall() }
        single { DevAinUpdatePersistenceCountAPICall() }
    }

    private val discordBot by inject<DiscordBot>()

    override fun onInitialize() {
        loadKoinModules(devAinModule)
        discordBot.registerCommands(DevAinStatusCommand())
    }

    override fun getVersion(): String {
        return "built-in"
    }


}