package skywolf46.devain.controller.commands.discord.gpt

import arrow.core.*
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.utils.FileUpload
import org.koin.core.component.get
import skywolf46.devain.controller.api.requests.openai.GPTCompletionAPICall
import skywolf46.devain.model.rest.gpt.completion.OpenAIGPTMessage
import skywolf46.devain.model.rest.gpt.completion.request.OpenAIGPTRequest
import skywolf46.devain.model.rest.gpt.completion.response.OpenAIGPTResponse
import skywolf46.devain.platform.discord.DiscordCommand
import java.text.DecimalFormat
import kotlin.math.round

class SimpleGPTCommand(
    private val command: String,
    private val description: String,
    private val model: String? = null
) :
    DiscordCommand() {
    companion object {
        const val DEFAULT_MODEL = "gpt-4"
        private val priceInfo = mapOf("gpt-4" to 0.06, "gpt-3.5-turbo" to 0.002)
        private const val dollarToWonMultiplier = 1322.50
        private val decimalFormat = DecimalFormat("#,###")
    }

    private val apiCall = get<GPTCompletionAPICall>()

    override fun createCommandInfo(): Pair<String, CommandData> {
        val commandData =
            Commands.slash(command, description)
        if (model == null) {
            commandData.addOption(OptionType.STRING, "model", "")
        }
        commandData.addOption(OptionType.STRING, "contents", "ChatGPT-3.5에게 질문할 내용입니다.", true)
            .addOption(
                OptionType.NUMBER,
                "temperature",
                "모델의 temperature 값을 설정합니다. 값이 낮을수록 결정적이 되며, 높을수록 더 많은 무작위성이 가해집니다. (기본 1, 0-1.5내의 소수)",
                false
            )
            .addOption(
                OptionType.NUMBER,
                "top_p",
                "모델의 top_p 값을 설정합니다. 값이 낮을수록 토큰의 샘플링 범위가 낮아집니다. (기본 1, 0-1.5내의 소수)",
                false
            )
            .addOption(
                OptionType.INTEGER,
                "max_token",
                "최대 토큰 개수를 설정합니다.",
                false
            )
            .addOption(
                OptionType.NUMBER,
                "presence_penalty",
                "모델의 중복 주제 패널티를 조정합니다. 높을수록, 새 주제(토큰)에 관해 이야기할 확률이 높아집니다. (기본 0, -2.0-2.0내의 소수)",
                false
            )
            .addOption(
                OptionType.NUMBER,
                "frequency_penalty",
                "모델의 중복 빈도 패널티를 조정합니다. 높을수록, 같은 말(토큰)을 반복하지 않을 확률이 높아집니다. (기본 0, -2.0-2.0내의 소수)",
                false
            )
            .addOption(OptionType.BOOLEAN, "hide-prompt", "결과 창에서 프롬프트를 숨깁니다. 명령어 클릭은 숨겨지지 않습니다.", false)
            .addOption(OptionType.STRING, "base-prompt", "해당 프롬프트의 기반이 될 프롬프트입니다. AI는 해당 내용을 기반으로 답변을 시도합니다.", false)
        return command to commandData
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        event.defer { _, hook ->
            val request = OpenAIGPTRequest(
                model ?: event.getOption("model")!!.asString,
                event.getOption("base-prompt")?.asString?.let {
                    listOf(
                        OpenAIGPTMessage(OpenAIGPTMessage.Role.ASSISTANT, it),
                        OpenAIGPTMessage(OpenAIGPTMessage.Role.USER, event.getOption("contents")!!.asString))
                } ?: listOf(OpenAIGPTMessage(OpenAIGPTMessage.Role.USER, event.getOption("contents")!!.asString)),
                1,
                event.getOption("temperature")?.asDouble.toOption(),
                event.getOption("top_p")?.asDouble.toOption(),
                event.getOption("max_token")?.asInt.toOption(),
                event.getOption("presence_penalty")?.asDouble.toOption(),
                event.getOption("frequency_penalty")?.asDouble.toOption(),
                event.getOption("hide-prompt")?.asBoolean ?: false
            )
            apiCall.call(
                request
            ).onLeft {
                hook.sendMessage(it.getErrorMessage()).queue()
            }.onRight { result ->
                val text = buildReturnValue(event, request, result)
                if (text.length >= 2000) {
                    hook.sendFiles(FileUpload.fromData(text.toByteArray(), "response.txt")).queue()
                } else {
                    hook.sendMessage(text).queue()
                }
            }
        }
    }


    private fun buildReturnValue(
        event: SlashCommandInteractionEvent,
        request: OpenAIGPTRequest,
        result: OpenAIGPTResponse
    ): String {
        val builder = StringBuilder()
        appendApiInfo(event, builder, request, result)
        if (!request.hidePrompt)
            appendRequest(builder, request)
        appendResult(builder, result)
        return builder.toString()
    }

    private fun appendModel(event: SlashCommandInteractionEvent, builder: StringBuilder, request: OpenAIGPTRequest) {
        builder.append("└ 모델: ${request.modelName}").appendNewLine()
        appendParameter(event, builder, request)
    }

    private fun appendParameter(
        event: SlashCommandInteractionEvent,
        builder: StringBuilder,
        request: OpenAIGPTRequest
    ) {
        request.temperature.tap {
            builder.append("  └ Temperature: $it").appendNewLine()
        }
        request.top_p.tap {
            builder.append("  └ top_p: $it").appendNewLine()
        }
        request.presencePenalty.tap {
            builder.append("  └ Presence Penalty: $it").appendNewLine()
        }
        request.frequencyPenalty.tap {
            builder.append("  └ Frequency Penalty: $it").appendNewLine()
        }

        request.maxTokens.tap {
            builder.append("  └ Max tokens: ${decimalFormat.format(it)}").appendNewLine()
        }

        if (request.hidePrompt) {
            builder.append("  └ Prompt hidden").appendNewLine()
        }
    }

    private fun appendApiInfo(
        event: SlashCommandInteractionEvent,
        builder: StringBuilder,
        request: OpenAIGPTRequest,
        result: OpenAIGPTResponse
    ) {
        builder.append("**API 상세**:").appendNewLine(1)
        appendModel(event, builder, request)
        builder.append("└ API 소모: ${result.usage.totalToken}토큰")
        if (request.modelName in priceInfo) {
            val token = result.usage.totalToken.toDouble() / 1000.0
            val price = round(priceInfo[request.modelName]!! * token * 10000.0) / 10000.0
            builder.append(" ($${price}, 추산치 ${round(dollarToWonMultiplier * price * 1000) / 1000.0}원)")
        }
        builder.appendNewLine()
        builder.append("└ API 응답 시간: ${decimalFormat.format(System.currentTimeMillis() - request.createdOn)}ms")
            .appendNewLine(2)
    }

    private fun appendRequest(builder: StringBuilder, request: OpenAIGPTRequest) {
        builder.append("**요청:** \n${request.messages.last().content}")
        builder.appendNewLine(2)
    }

    private fun appendResult(builder: StringBuilder, result: OpenAIGPTResponse) {
        builder.append("**응답:** \n${result.answers[0].message.content}")
    }

    private fun StringBuilder.appendNewLine(count: Int = 1) {
        append("\n".repeat(count))
    }

    override suspend fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        when (event.name) {
            "model" -> {

            }
        }
    }
}