package skywolf46.devain.controller.commands.discord.deepl

import arrow.core.None
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.koin.core.component.get
import skywolf46.devain.controller.api.requests.deepl.DeepLTranslationAPICall
import skywolf46.devain.model.api.deepl.translation.DeepLTranslateRequest
import skywolf46.devain.platform.discord.ImprovedDiscordCommand

class DeepLKoreanTranslationCommand : ImprovedDiscordCommand("deepl-ko", "DeepL 번역 API를 사용해 주어진 텍스트를 한국어로 번역합니다.") {
    private val apiCall = get<DeepLTranslationAPICall>()

    override fun modifyCommandData(options: SlashCommandData) {
        options.addOption(
            OptionType.STRING,
            "text",
            "번역할 텍스트를 지정합니다.",
            true
        )
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        event.defer { _, hook ->
            apiCall.call(
                DeepLTranslateRequest(
                    None,
                    "Korean",
                    event.getOption("text")!!.asString
                )
            ).onLeft {
                hook.sendMessage(it.getErrorMessage()).queue()
            }.onRight {
                hook.sendMessage(it.translationResult).queue()
            }
        }

    }
}