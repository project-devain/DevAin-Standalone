package skywolf46.devain.controller.commands.discord.cohere

import arrow.core.toOption
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.koin.core.component.inject
import skywolf46.devain.controller.api.cohere.CohereGenerationAPICall
import skywolf46.devain.model.api.cohere.CohereGenerationRequest
import skywolf46.devain.platform.discord.ImprovedDiscordCommand

class CohereCommand : ImprovedDiscordCommand("cohere") {
    private val apiCall by inject<CohereGenerationAPICall>()

    override fun modifyCommandData(options: SlashCommandData) {
        options.addOption(OptionType.STRING, "prompt", "요청 프롬프트입니다.", true)
        options.addOption(OptionType.INTEGER, "max-tokens", "최대 토큰입니다.")
        options.addOption(
            OptionType.NUMBER,
            "temperature",
            "temperature 값입니다. 낮을수록 예측 가능한 값을, 높을수록 예측 불가능한 결과를 제공합니다. (기본 0.75, 0.0 ~ 5.0)"
        )
        options.addOption(
            OptionType.INTEGER,
            "top_k",
            "top_k 값입니다. 각 단계에서 생성할 가능성이 높은 상위 k개의 값만 고려되도록 합니다. (기본 0, 0 ~ 500)"
        )
        options.addOption(
            OptionType.NUMBER,
            "top_p",
            "top_p 값입니다. 각 단계에서 총 확률 질량이 p인 가장 가능성이 높은 토큰만 고려되도록 합니다. (기본 0.0, 0.0 ~ 0.99)"
        )
        options.addOption(
            OptionType.NUMBER,
            "frequency_penalty",
            "frequency_penalty 값입니다. 높을수록, 이전에 반복된 토큰에 대하여 횟수에 비례해 더 강력한 패널티를 가합니다. (기본 0.0, 0.0 ~ 1.0)"
        )
        options.addOption(
            OptionType.NUMBER,
            "presence_penalty",
            "presence_penalty 값입니다. 높을수록, 이전에 반복된 값에 대해 동일한 패널티를 가합니다. (기본 0.0, 0.0 ~ 1.0)"
        )
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        val request = CohereGenerationRequest(
            event.getOption("prompt")!!.asString,
            "command",
            1,
            false,
            maxTokens = (event.getOption("max-tokens")?.asInt ?: 1024).toOption(),
            temperature = event.getOption("temperature")?.asDouble.toOption(),
            top_k = event.getOption("top_k")?.asInt.toOption(),
            top_p = event.getOption("top_p")?.asDouble.toOption(),
            frequencyPenalty = event.getOption("frequency_penalty")?.asDouble.toOption(),
            presencePenalty = event.getOption("presence_penalty")?.asDouble.toOption(),
        )
        event.defer { _, hook ->
            apiCall.call(request).fold({
                hook.sendMessage("오류가 발생했습니다. ${it.getErrorMessage()}").queue()
            }) {
                hook.sendMessage(it.generations[0].text).queue()
            }
        }
    }
}