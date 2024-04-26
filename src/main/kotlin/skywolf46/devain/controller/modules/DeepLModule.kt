package skywolf46.devain.controller.modules

import org.koin.core.component.inject
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import skywolf46.devain.KEY_DEEPL_PROCEED_COUNT
import skywolf46.devain.KEY_DEEPL_PROCEED_TOKEN
import skywolf46.devain.apicall.certainly
import skywolf46.devain.apicall.networking.GetRequest
import skywolf46.devain.controller.api.requests.deepl.DeepLTranslationAPICall
import skywolf46.devain.controller.api.requests.devain.DevAinPersistenceCountAPICall
import skywolf46.devain.controller.commands.discord.deepl.DeepLKoreanTranslationCommand
import skywolf46.devain.controller.commands.discord.deepl.DeepLSimpleTranslationCommand
import skywolf46.devain.controller.commands.discord.deepl.DeepLTranslationCommand
import skywolf46.devain.controller.commands.discord.deepl.ModalDeepLKoreanTranslationCommand
import skywolf46.devain.model.data.config.APITokenElement
import skywolf46.devain.model.data.config.fetchSharedDocument
import skywolf46.devain.platform.discord.DiscordBot
import skywolf46.devain.platform.plugin.PluginModule

class DeepLModule() : PluginModule("DeepL Integration") {
    private val apiCall by inject<DevAinPersistenceCountAPICall>()

    private val discordBot by inject<DiscordBot>()

    override fun onInitialize() {
        document.fetchSharedDocument<APITokenElement>(pluginName) { config ->
            loadKoinModules(module {
                single { DeepLTranslationAPICall(config.apiToken) }
            })
            discordBot.registerCommands(
                DeepLTranslationCommand(),
                DeepLSimpleTranslationCommand(),
                DeepLKoreanTranslationCommand(),
                ModalDeepLKoreanTranslationCommand()
            )
        }

    }

    override suspend fun getStatistics(): Map<String, List<PluginStatistics>> {
        return mapOf(
            "DeepL Translation" to listOf(
                PluginStatistics("총 번역 횟수", "%,d".format(apiCall.certainly(GetRequest(KEY_DEEPL_PROCEED_COUNT)).value)),
                PluginStatistics(
                    "번역 완료 문자열",
                    "%,d".format(apiCall.certainly(GetRequest(KEY_DEEPL_PROCEED_TOKEN)).value)
                )
            )
        )
    }

    override fun getVersion(): String {
        return "built-in"
    }
}