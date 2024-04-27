package skywolf46.devain.controller.commands.discord.groq

import arrow.core.toOption
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.utils.FileUpload
import org.koin.core.component.get
import skywolf46.devain.controller.api.requests.groq.GroqAPICall
import skywolf46.devain.controller.commands.discord.openai.GPTCommand
import skywolf46.devain.model.api.openai.completion.OpenAIGPTMessage
import skywolf46.devain.model.api.openai.completion.OpenAIGPTRequest
import skywolf46.devain.model.api.openai.completion.OpenAIGPTResponse
import java.text.DecimalFormat

class SimpleGroqCommand(
    command: String,
    description: String,
    private val model: String,
    private val modelDisplay: String
) : GPTCommand(command, description) {
    private val decimalFormat = DecimalFormat("#,###")

    private val apiCall = get<GroqAPICall>()

    override fun modifyCommandData(options: SlashCommandData) {
        options.addOption(OptionType.STRING, "contents", "${modelDisplay}에게 질문할 내용입니다.", true)
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
            .addOption(OptionType.INTEGER, "best_of", "모델의 최고 결과물 중 몇 개를 선택할지를 설정합니다. (기본 1, 1-10)", false)
            .addOption(OptionType.BOOLEAN, "hide-prompt", "결과 창에서 프롬프트를 숨깁니다. 명령어 클릭은 숨겨지지 않습니다.", false)
            .addOption(OptionType.STRING, "base-prompt", "해당 프롬프트의 기반이 될 프롬프트입니다. AI는 해당 내용을 기반으로 답변을 시도합니다.", false)
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        event.defer { _, hook ->
            val request = OpenAIGPTRequest(
                model,
                event.getOption("base-prompt")?.asString?.let {
                    mutableListOf(
                        OpenAIGPTMessage(OpenAIGPTMessage.Role.ASSISTANT, it.toOption()),
                        OpenAIGPTMessage(OpenAIGPTMessage.Role.USER, event.getOption("contents")!!.asString.toOption())
                    )
                } ?: mutableListOf(
                    OpenAIGPTMessage(
                        OpenAIGPTMessage.Role.USER,
                        event.getOption("contents")!!.asString.toOption()
                    )
                ),
                1,
                event.getOption("temperature")?.asDouble.toOption(),
                event.getOption("top_p")?.asDouble.toOption(),
                event.getOption("best_of")?.asInt.toOption(),
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
                if (isEmbedCompatible(request, result)) {
                    runCatching {
                        hook.sendMessageEmbeds(buildEmbedded(request, result)).queue()
                    }.onFailure { exception ->
                        exception.printStackTrace()
                        hook.sendMessage("${exception.javaClass.name}: ${exception.message}")
                    }
                    return@onRight
                }
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
        appendParameter( builder, request)
    }

    private fun appendParameter(
        builder: StringBuilder,
        request: OpenAIGPTRequest
    ) {
        request.temperature.onSome {
            builder.append("  └ Temperature: $it").appendNewLine()
        }
        request.top_p.onSome {
            builder.append("  └ top_p: $it").appendNewLine()
        }
        request.presencePenalty.onSome {
            builder.append("  └ Presence Penalty: $it").appendNewLine()
        }
        request.frequencyPenalty.onSome {
            builder.append("  └ Frequency Penalty: $it").appendNewLine()
        }

        request.maxTokens.onSome {
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
        builder.appendNewLine()
        builder.append("└ API 응답 시간: ${decimalFormat.format(System.currentTimeMillis() - request.createdOn)}ms")
            .appendNewLine(2)
    }

    private fun appendRequest(builder: StringBuilder, request: OpenAIGPTRequest) {
        builder.append("**요청:** \n${request.messages.last().content.find { it.first == "text" }?.second}")
        builder.appendNewLine(2)
    }

    private fun appendResult(builder: StringBuilder, result: OpenAIGPTResponse) {
        builder.append("**응답:** \n${result.answers[0].message.content.find { it.first == "text" }?.second}")
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