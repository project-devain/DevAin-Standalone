package skywolf46.devain.controller.commands.discord.deepl

import arrow.core.toOption
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.koin.core.component.get
import skywolf46.devain.controller.api.requests.deepl.DeepLTranslationAPICall
import skywolf46.devain.model.rest.deepl.translation.DeepLTranslateRequest
import skywolf46.devain.platform.discord.ImprovedDiscordCommand

class DeepLSimpleTranslationCommand : ImprovedDiscordCommand("deepl-simple", "DeepL 번역 API를 사용해 주어진 텍스트를 번역합니다.") {
    private val allowedTranslation = mapOf("ko" to "Korean", "en" to "English", "jp" to "Japanese", "cn" to "Chinese")
    private val apiCall = get<DeepLTranslationAPICall>()

    override fun modifyCommandData(options: SlashCommandData) {
        options.addCompletableOption(
            "target", "번역될 결과 언어를 지정합니다.", true
        ) {
            allowedTranslation.keys.toList()
        }.addOption(
            OptionType.STRING, "text", "번역할 텍스트를 지정합니다.", true
        ).addCompletableOption(
            "source", "번역될 텍스트의 언어를 지정합니다.", false
        ) {
            allowedTranslation.keys.toList()
        }
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        event.defer { _, hook ->
            apiCall.call(
                DeepLTranslateRequest(
                    event.getOption("source")?.asString?.let { allowedTranslation[it] ?: it }.toOption(),
                    event.getOption("target")!!.asString.let { allowedTranslation[it] ?: it },
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