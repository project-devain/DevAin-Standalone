package skywolf46.devain.controller.commands.discord.openai

import arrow.core.Option
import arrow.core.toOption
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.koin.core.component.inject
import skywolf46.devain.controller.api.requests.openai.DallEAPICall
import skywolf46.devain.model.api.openai.dalle.DallERequest
import skywolf46.devain.platform.discord.ImprovedDiscordCommand
import java.awt.Color

class DallEGenerationCommand : ImprovedDiscordCommand("dalle", "OpenAI DallE를 사용해 이미지를 생성합니다.", "dalle".toOption()) {
    private val apiCall by inject<DallEAPICall>()
    override fun modifyCommandData(options: SlashCommandData) {
        options.addOption(
            OptionType.STRING,
            "prompt",
            "이미지를 생성할 프롬프트를 지정합니다.",
            true
        )
        options.addCompletableOption(
            "size",
            "이미지의 크기를 지정합니다.",
            false
        ) {
            DallERequest.ImageSize.values().map { it.name.lowercase() }.toList()
        }
        options.addCompletableOption("style", "이미지의 스타일을 지정합니다.", false) {
            listOf("vivid", "natural")
        }
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        event.defer { _, hook ->
            val prompt = event.getOption("prompt")!!.asString
            apiCall.call(DallERequest(
                prompt,
                event.getOption("size")?.asString?.let { DallERequest.ImageSize.valueOf(it.uppercase()) }
                    ?: DallERequest.ImageSize.X1024,
                DallERequest.ResponseType.URL,
                1,
                Option.fromNullable(event.getOption("style")?.asString)
            )).onLeft {
                hook.sendMessage(it.getErrorMessage()).queue()
            }.onRight {
                hook.sendMessageEmbeds(
                    EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle("Reqeust complete - DallE")
                        .setDescription(prompt)
                        .setImage(it.images[0].url.getOrNull()!!)
                        .build()
                ).queue()
            }
        }
    }
}