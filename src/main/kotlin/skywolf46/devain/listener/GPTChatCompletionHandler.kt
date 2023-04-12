package skywolf46.devain.listener

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.utils.FileUpload
import skywolf46.devain.config.BotConfig
import skywolf46.devain.data.gpt.GPTRequest
import skywolf46.devain.data.gpt.ParsedGPTResult
import skywolf46.devain.util.OpenAiRequest
import java.text.DecimalFormat
import kotlin.math.round

object GPTChatCompletionHandler {
    private val priceInfo = mapOf("gpt-4" to 0.06, "gpt-3.5-turbo" to 0.002)
    private const val dollarToWonMultiplier = 1322.50
    private val decimalFormat = DecimalFormat("#,###")

    fun handle(config: BotConfig, model: String, event: SlashCommandInteractionEvent) {
        event.deferReply(false).queue { hook ->
            processRequest(
                hook,
                GPTRequest(
                    model,
                    event.getOption("contents")!!.asString,
                    event.getOption("temperature")?.asDouble ?: -1.0,
                    event.getOption("top_p")?.asDouble ?: -1.0,
                    event.getOption("max-token")?.asInt ?: -1,
                    event.getOption("hide-prompt")?.asBoolean ?: false
                ),
                config
            )
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun processRequest(
        hook: InteractionHook,
        request: GPTRequest,
        config: BotConfig
    ) {
        checkRestriction(request).onLeft {
            hook.sendMessage(it).queue()
            return
        }
        GlobalScope.launch {
            kotlin.runCatching {
                val result = OpenAiRequest.requestGpt(config.openAIToken, request)
                result.onLeft {
                    hook.sendMessage("OpenAI API가 오류를 반환하였습니다 : $it").queue()
                }.onRight {
                    val text = buildReturnValue(request, it)
                    if (text.length >= 2000) {
                        hook.sendFiles(FileUpload.fromData(text.toByteArray(), "answer.txt")).queue()
                    } else {
                        hook.sendMessage(text).queue()
                    }
                }
            }.onFailure {
                hook.sendMessage("OpenAI API와 통신 중 오류가 발생하였습니다. (${it.javaClass.simpleName} : ${it.message})").queue()
                it.printStackTrace()
            }
        }
    }

    private fun checkRestriction(request: GPTRequest): Either<String, Unit> {
        if (request.temperature != -1.0 && request.temperature !in 0.0..1.5) {
            return "잘못된 파라미터 값이 전달되었습니다 : temperature 파라미터는 0.0과 1.5 사이여야만 합니다.".left()
        }
        if (request.top_p != -1.0 && request.top_p !in 0.0..1.5) {
            return "잘못된 파라미터 값이 전달되었습니다 : top_p 파라미터는 0.0과 1.5 사이여야만 합니다.".left()
        }
        if (request.maxToken != -1 && request.maxToken < 10) {
            return "잘못된 파라미터 값이 전달되었습니다 : maxToken 파라미터는 10 이상이여야만 합니다.".left()
        }
        return Unit.right()
    }

    private fun buildReturnValue(request: GPTRequest, result: ParsedGPTResult): String {
        val builder = StringBuilder()
        appendApiInfo(builder, request, result)
        if (!request.hideRequest)
            appendRequest(builder, request)
        appendResult(builder, result)
        return builder.toString()
    }

    private fun appendModel(builder: StringBuilder, request: GPTRequest) {
        builder.append("└ 모델: ${request.model}").appendNewLine()
        appendParameter(builder, request)
    }

    private fun appendParameter(
        builder: StringBuilder,
        request: GPTRequest
    ) {
        if (request.temperature != -1.0) {
            builder.append("  └ Temperature: ${request.temperature}").appendNewLine()
        }
        if (request.top_p != -1.0) {
            builder.append("  └ top_p: ${request.top_p}").appendNewLine()
        }

        if (request.maxToken != -1) {
            builder.append("  └ Max tokens: ${decimalFormat.format(request.maxToken)}").appendNewLine()
        }
        if (request.hideRequest) {
            builder.append("  └ Prompt hidden").appendNewLine()
        }
//        if (request.temperature == -1.0 && request.top_p == -1.0 && request.maxToken == -1 && !request.hideRequest) {
//            return
//        }
//        builder.append(" (")
//        if (request.temperature != -1.0) {
//            builder.append("Temperature ${request.temperature}")
//        }
//        if (request.top_p != -1.0) {
//            if (!builder.endsWith('(')) {
//                builder.append(", ")
//            }
//            builder.append("top_p ${request.top_p}")
//        }
//        if (request.maxToken != -1) {
//            if (!builder.endsWith('(')) {
//                builder.append(", ")
//            }
//            builder.append("Max token ${decimalFormat.format(request.maxToken)}")
//        }
//
//        if (request.hideRequest) {
//            if (!builder.endsWith('(')) {
//                builder.append(", ")
//            }
//            builder.append("Prompt hidden")
//        }
//        builder.append(")")
    }

    private fun appendApiInfo(builder: StringBuilder, request: GPTRequest, result: ParsedGPTResult) {
        builder.append("**API 상세**:").appendNewLine(1)
        appendModel(builder, request)
        builder.append("└ API 소모: ${result.tokenUsage.totalTokens}토큰")
        if (request.model in priceInfo) {
            val token = result.tokenUsage.totalTokens.toDouble() / 1000.0
            val price = round(priceInfo[request.model]!! * token * 10000.0) / 10000.0
            builder.append(" ($${price}, 추산치 ${round(dollarToWonMultiplier * price * 1000) / 1000.0}원)")
        }
        builder.appendNewLine()
        builder.append("└ API 응답 시간: ${decimalFormat.format(System.currentTimeMillis() - request.timeStamp)}ms").appendNewLine(2)
    }

    private fun appendRequest(builder: StringBuilder, request: GPTRequest) {
        builder.append("**요청:** \n${request.contents}")
        builder.appendNewLine(2)
    }

    private fun appendResult(builder: StringBuilder, result: ParsedGPTResult) {
        builder.append("**응답:** \n${result.choices[0].answer}")
    }

    private fun StringBuilder.appendNewLine(count: Int = 1) {
        append("\n".repeat(count))
    }
}