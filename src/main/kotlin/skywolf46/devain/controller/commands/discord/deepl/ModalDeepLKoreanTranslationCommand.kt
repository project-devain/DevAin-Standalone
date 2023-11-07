package skywolf46.devain.controller.commands.discord.deepl

import arrow.core.None
import arrow.core.toOption
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import org.koin.core.component.get
import skywolf46.devain.controller.api.requests.deepl.DeepLTranslationAPICall
import skywolf46.devain.model.api.deepl.translation.DeepLTranslateRequest
import skywolf46.devain.platform.discord.ImprovedDiscordCommand

class ModalDeepLKoreanTranslationCommand :
    ImprovedDiscordCommand("modal-deepl-ko", "DeepL 번역 API를 사용해 주어진 텍스트를 한국어로 번역합니다.", "modal-deepl-ko".toOption()) {
    private val apiCall = get<DeepLTranslationAPICall>()

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        event.listenModal(createModal("DeepL -> Korean Translation") {
            it.addActionRow(TextInput.create("text", "번역할 텍스트를 입력하세요.", TextInputStyle.PARAGRAPH).build())
        }) { modal ->
            runBlocking {
                apiCall.call(
                    DeepLTranslateRequest(
                        None,
                        "Korean",
                        modal.interaction.getValue("text")!!.asString
                    )
                ).onLeft {
                    modal.reply(it.getErrorMessage()).queue()
                }.onRight {
                    modal.reply(it.translationResult).queue()
                }
            }
        }

    }
}