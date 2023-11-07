package skywolf46.devain.controller.commands.discord.openai

import arrow.core.*
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.utils.FileUpload
import org.koin.core.component.inject
import skywolf46.devain.controller.api.requests.openai.GPTCompletionAPICall
import skywolf46.devain.model.api.openai.completion.OpenAIFunctionKey
import skywolf46.devain.model.api.openai.completion.OpenAIGPTMessage
import skywolf46.devain.model.api.openai.completion.OpenAIGPTRequest
import skywolf46.devain.model.api.openai.completion.OpenAIGPTResponse
import skywolf46.devain.platform.discord.ImprovedDiscordCommand

open class GPTBaseCommand(
    commandName: String,
    description: String,
    val supportModal: Boolean,
    val supportFunctions: Boolean,
    val model: Option<String> = None
) : ImprovedDiscordCommand(commandName, description, commandName.toOption()) {
    private val apiCall by inject<GPTCompletionAPICall>()


    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        if (supportModal) {
            requestFromModal(event)
        } else {
            requestFromCommand(event)
        }
    }

    private fun requestFromCommand(event: SlashCommandInteractionEvent) {
        event.defer { _, hook ->
            val request = buildRequest(event, None)
            val response = apiCall.call(request).getOrElse {
                hook.sendMessage(it.getErrorMessage())
                return@defer
            }
            if (isEmbedCompatible(request, response)) {
                hook.sendMessageEmbeds(buildEmbed(request, response)).queue()
            } else {
                buildStringResult(request, response).apply {
                    if (length <= 2000) {
                        hook.sendMessage(this).queue()
                        return@defer
                    }
                }
                hook.sendFiles(
                    FileUpload.fromData(
                        StringBuilder().buildFileStringResult(request, response).toByteArray(), "result.txt"
                    )
                ).queue()
            }
        }
    }

    private fun requestFromModal(event: SlashCommandInteractionEvent) {
        event.listenModal(createModal("Prompt Input") {
            it.addActionRow(TextInput.create("base-prompt", "사전 프롬프트", TextInputStyle.SHORT).build())
            it.addActionRow(TextInput.create("prompt", "프롬프트", TextInputStyle.PARAGRAPH).build())
        }) { modalEvent ->
            val request = buildRequest(event, modalEvent.interaction.getValue("prompt")!!.asString.toOption())
            val response = apiCall.call(request).getOrElse {
                modalEvent.reply(it.getErrorMessage())
                return@listenModal
            }
            if (isEmbedCompatible(request, response)) {
                modalEvent.replyEmbeds(buildEmbed(request, response)).queue()
            } else {
                buildStringResult(request, response).apply {
                    if (length <= 2000) {
                        modalEvent.reply(this).queue()
                        return@listenModal
                    }
                }
                modalEvent.replyFiles(
                    FileUpload.fromData(
                        StringBuilder().buildFileStringResult(request, response).toByteArray(), "result.txt"
                    )
                ).queue()
            }


        }
    }

    private fun buildEmbed(request: OpenAIGPTRequest, response: OpenAIGPTResponse): MessageEmbed {
        TODO()
    }

    private fun buildStringResult(request: OpenAIGPTRequest, response: OpenAIGPTResponse): String {
        val builder = StringBuilder()
        return builder.toString()
    }

    private fun StringBuilder.buildFileStringResult(request: OpenAIGPTRequest, response: OpenAIGPTResponse): String {
        val builder = StringBuilder()
        return builder.toString()
    }

    fun isEmbedCompatible(request: OpenAIGPTRequest, response: OpenAIGPTResponse): Boolean {
        val requestMessage = request.messages.find { it.role == OpenAIGPTMessage.Role.USER }!!
        val responseMessage = response.answers.last()
        return (request.hidePrompt || requestMessage.content.orNull()
            .toString().length < 4096) && (responseMessage.message.content.orNull().toString().length < 1024 - 6)
    }

    final override fun modifyCommandData(options: SlashCommandData) {
        model.tapNone {
            options.addOption(OptionType.STRING, "model", "프롬프트를 생성할 모델을 지정합니다.")
        }
        if (!supportModal) {
            options.addOption(OptionType.STRING, "prompt", model.getOrElse { "지정된 모델" } + "에게 질문할 내용입니다.")
        }
        options.addOption(
            OptionType.NUMBER,
            "temperature",
            "모델의 temperature 값을 설정합니다. 값이 낮을수록 결정적이 되며, 높을수록 더 많은 무작위성이 가해집니다. (기본 1, 0-1.5내의 소수)",
            false
        )
        options.addOption(
            OptionType.NUMBER,
            "top_p",
            "모델의 top_p 값을 설정합니다. 값이 낮을수록 토큰의 샘플링 범위가 낮아집니다. (기본 1, 0-1.5내의 소수)",
            false
        )
        options.addOption(
            OptionType.INTEGER,
            "max_token",
            "최대 토큰 개수를 설정합니다.",
            false
        )
        options.addOption(
            OptionType.NUMBER,
            "presence_penalty",
            "모델의 중복 주제 패널티를 조정합니다. 높을수록, 새 주제(토큰)에 관해 이야기할 확률이 높아집니다. (기본 0, -2.0-2.0내의 소수)",
            false
        )
        options.addOption(
            OptionType.NUMBER,
            "frequency_penalty",
            "모델의 중복 빈도 패널티를 조정합니다. 높을수록, 같은 말(토큰)을 반복하지 않을 확률이 높아집니다. (기본 0, -2.0-2.0내의 소수)",
            false
        )
        options.addOption(OptionType.BOOLEAN, "hide-prompt", "결과 창에서 프롬프트를 숨깁니다. 명령어 클릭은 숨겨지지 않습니다.", false)
        options.addOption(OptionType.STRING, "base-prompt", "해당 프롬프트의 기반이 될 프롬프트입니다. AI는 해당 내용을 기반으로 답변을 시도합니다.", false)
        if (supportFunctions) {
            options.addOption(OptionType.BOOLEAN, "show-trace", "사용된 펑션 콜 스택 트레이스를 출력할지의 여부를 설정합니다.", false)
        }
    }


    private fun buildRequest(
        event: SlashCommandInteractionEvent,
        basePrompt: Option<String>,
        prompt: Option<String> = None
    ): OpenAIGPTRequest {
        val messageListBuilt = acquirePrompts(event, prompt, basePrompt = basePrompt)
        return OpenAIGPTRequest(
            model.getOrElse { event.getOption("model")!!.asString },
            messageListBuilt,
            1,
            event.getOption("temperature")?.asDouble.toOption(),
            event.getOption("top_p")?.asDouble.toOption(),
            None,
            event.getOption("max_token")?.asInt.toOption(),
            event.getOption("presence_penalty")?.asDouble.toOption(),
            event.getOption("frequency_penalty")?.asDouble.toOption(),
            event.getOption("hide-prompt")?.asBoolean ?: false,
            if (supportFunctions) event.getOption("show-trace")?.asBoolean ?: false else false,
            acquireFunctions(event)
        )
    }

    protected open fun acquirePrompts(
        event: SlashCommandInteractionEvent,
        prompt: Option<String>,
        basePrompt: Option<String> = None
    ): MutableList<OpenAIGPTMessage> {
        return mutableListOf<OpenAIGPTMessage>().apply {
            (basePrompt.orNull() ?: event.getOption("base-prompt")?.asString)?.let { basePrompt ->
                add(OpenAIGPTMessage(OpenAIGPTMessage.Role.USER_PRECONDITION, basePrompt.toOption()))
            }
            add(
                OpenAIGPTMessage(
                    OpenAIGPTMessage.Role.USER,
                    prompt.getOrElse { event.getOption("prompt")!!.asString }.toOption()
                )
            )
        }
    }

    protected open fun acquireFunctions(event: SlashCommandInteractionEvent): Option<List<OpenAIFunctionKey>> {
        if (!supportFunctions)
            return None
        return listOf<OpenAIFunctionKey>().toOption()
    }
}