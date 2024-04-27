package skywolf46.devain.controller.commands.discord.anthropic

import arrow.core.toOption
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.koin.core.component.inject
import skywolf46.devain.controller.api.requests.anthropic.ClaudeAPICall
import skywolf46.devain.model.api.anthropic.ClaudeGenerationRequest
import skywolf46.devain.model.api.anthropic.ClaudeMessage
import skywolf46.devain.platform.discord.ImprovedDiscordCommand

class ClaudeCommand : ImprovedDiscordCommand("claude") {
    private val apiCall by inject<ClaudeAPICall>()

    override fun modifyCommandData(options: SlashCommandData) {
        options.addOption(OptionType.STRING, "prompt", "요청 프롬프트입니다.", true)
        options.addOption(OptionType.INTEGER, "max-tokens", "최대 토큰입니다.")
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        val request = ClaudeGenerationRequest(
            "claude-3-opus-20240229",
            listOf(
                ClaudeMessage(
                    ClaudeMessage.ClaudeGenerationRole.USER, event.getOption("prompt")!!.asString

                )
            ),
            4096.toOption()
        )
        event.defer { _, hook ->
            apiCall.call(request).fold({
                hook.sendMessage("오류가 발생했습니다. ${it.getErrorMessage()}").queue()
            }) {
                hook.sendMessage(it.message.joinToString("\n")).queue()
            }
        }
    }
}