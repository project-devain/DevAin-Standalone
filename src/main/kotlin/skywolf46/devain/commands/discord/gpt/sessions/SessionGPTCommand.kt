package skywolf46.devain.commands.discord.gpt.sessions

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.utils.FileUpload
import org.koin.core.component.inject
import skywolf46.devain.config.BotConfig
import skywolf46.devain.data.parsed.gpt.GPTRequest
import skywolf46.devain.data.parsed.gpt.ParsedGPTResult
import skywolf46.devain.data.parsed.gpt.sessions.ChattingSession
import skywolf46.devain.data.storage.ChattingSessionStorage
import skywolf46.devain.data.storage.PresetStorage
import skywolf46.devain.data.storage.SessionTokenStorage
import skywolf46.devain.platform.discord.DiscordCommand
import skywolf46.devain.util.OpenAiRequest
import java.text.DecimalFormat
import kotlin.math.round

class SessionGPTCommand(
    private val command: String, private val description: String
) : DiscordCommand() {
    companion object {
        const val DEFAULT_MODEL = "gpt-4"
        private val priceInfo = mapOf("gpt-4" to 0.06, "gpt-3.5-turbo" to 0.002)
        private const val dollarToWonMultiplier = 1322.50
        private val decimalFormat = DecimalFormat("#,###")
    }

    private val chattingSessionStorage by inject<ChattingSessionStorage>()
    private val sessionTokenStorage by inject<SessionTokenStorage>()
    private val config by inject<BotConfig>()
    private val storage by inject<PresetStorage>()

    override fun createCommandInfo(): Pair<String, CommandData> {
        val commandData = Commands.slash(command, description)
        commandData.addOption(OptionType.STRING, "contents", "ChatGPT-3.5에게 질문할 내용입니다.", true)
            .addOption(OptionType.USER, "preset-user", "프롬프트를 불러올 사용자를 지정합니다. 공개 프리셋이 아닐 경우, 사용할 수 없습니다.", false)
            .addOption(
                OptionType.NUMBER,
                "temperature",
                "모델의 temperature 값을 설정합니다. 값이 낮을수록 결정적이 되며, 높을수록 더 많은 무작위성이 가해집니다. (기본 1, 0-1.5내의 소수)",
                false
            ).addOption(
                OptionType.NUMBER, "top_p", "모델의 top_p 값을 설정합니다. 값이 낮을수록 토큰의 샘플링 범위가 낮아집니다. (기본 1, 0-1.5내의 소수)", false
            ).addOption(OptionType.INTEGER, "max-token", "최대 토큰 개수를 설정합니다.", false)
            .addOption(OptionType.BOOLEAN, "hide-prompt", "결과 창에서 프롬프트를 숨깁니다. 명령어 클릭은 숨겨지지 않습니다.", false)
        return command to commandData
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        event.deferError { _, hook ->
            executeCommand(event, hook)
        }
    }

    private suspend fun executeCommand(
        event: SlashCommandInteractionEvent, hook: InteractionHook
    ): Either<String, Unit> {
        val request = buildRequest(event).getOrElse { return it.left() }
        val session = checkSession(event).getOrElse { return it.left() }
        checkSessionUsage(event, session, event.getOption("contents")!!.asString).getOrElse { return it.left() }
        return requestGpt(event, hook, session, request)
    }

    private fun checkSession(event: SlashCommandInteractionEvent): Either<String, ChattingSession> {
        return chattingSessionStorage.acquireSession(event.guild!!.idLong, event.member!!.idLong)
            .toEither { "채팅 세션이 존재하지 않습니다.\n/session-create 명령어로 세션을 초기화해주세요." }
    }

    private fun checkSessionUsage(
        event: SlashCommandInteractionEvent,
        session: ChattingSession,
        prompt: String,
    ): Either<String, Unit> {
        val toUse = session.getUsageWith(prompt)
        val amount = toUse * session.modelData.tokenMultiplier
        if (sessionTokenStorage.estimateToken(event.user.idLong).tokenAmount < amount) {
            return "## 세션 사용에 실패하였습니다 : 토큰이 부족합니다.\n잔여 토큰 ${sessionTokenStorage.estimateToken(event.user.idLong).tokenAmount}\n예상 프롬프트 토큰 $amount\n기존 세션 토큰 ${session.sessionUsage}\n예상 소모 최소 토큰 (${amount} + ${session.sessionUsage}) x ${session.modelData.tokenMultiplier} = ${(amount + session.sessionUsage) * session.modelData.tokenMultiplier} ".left()
        }
        return Unit.right()
    }

    private fun buildRequest(event: SlashCommandInteractionEvent): Either<String, GPTRequest> = either.eager {
        event.getOption("temperature")?.checkTemperatureRestriction()?.bind()
        event.getOption("top_p")?.checkSamplerParameterRestriction()?.bind()
        event.getOption("max_token")?.checkMaxTokenRestriction()?.bind()
        GPTRequest(
            "",
            event.getOption("contents")!!.asString,
            temperature = event.getOption("temperature")?.asDouble ?: -1.0,
            top_p = event.getOption("top_p")?.asDouble ?: -1.0,
            maxToken = event.getOption("max-token")?.asInt ?: -1,
            hideRequest = event.getOption("hide-prompt")?.asBoolean ?: false
        )
    }

    private fun OptionMapping.checkTemperatureRestriction(): Either<String, Unit> {
        return Either.catch {
            if (asDouble !in 0.0..1.5) {
                return "잘못된 파라미터 값이 전달되었습니다 : temperature 파라미터는 0.0과 1.5 사이여야만 합니다.".left()
            }
        }.mapLeft { "temperature 값이 숫자가 아닙니다." }
    }


    private fun OptionMapping.checkSamplerParameterRestriction(): Either<String, Unit> {
        return Either.catch {
            if (asDouble !in 0.0..1.0) {
                return "잘못된 파라미터 값이 전달되었습니다 : top_p 파라미터는 0.0과 1.0 사이여야만 합니다.".left()
            }
        }.mapLeft { "잘못된 파라미터 값이 전달되었습니다 : top_p 값이 숫자가 아닙니다." }
    }

    private fun OptionMapping.checkMaxTokenRestriction(): Either<String, Unit> {
        return Either.catch {
            if (asInt < 10) {
                return "잘못된 파라미터 값이 전달되었습니다 : maxToken 파라미터는 10 이상이여야만 합니다.".left()
            }
        }.mapLeft { "잘못된 파라미터 값이 전달되었습니다 : maxToken 값이 숫자가 아닙니다." }
    }

    private suspend fun requestGpt(
        event: SlashCommandInteractionEvent,
        hook: InteractionHook,
        session: ChattingSession,
        request: GPTRequest,
    ): Either<String, Unit> {
        if (!session.acquireSession()) {
            return "채팅 세션이 사용중에 있습니다.".left()
        }
        val result = OpenAiRequest.requestGpt(config.openAIToken, request, session = session).getOrElse {
            session.releaseSession()
            return it.left()
        }
        return sendResult(event, hook, request, result, session).right()
    }

    private fun sendResult(
        event: SlashCommandInteractionEvent,
        hook: InteractionHook,
        request: GPTRequest,
        result: ParsedGPTResult,
        session: ChattingSession,
    ) {
        val text = buildReturnValue(event, request, result, session)
        if (text.length >= 2000) {
            hook.sendFiles(FileUpload.fromData(text.toByteArray(), "answer.txt")).queue()
        } else {
            hook.sendMessage(text).queue()
        }
        sessionTokenStorage.withdraw(
            event.user.idLong, (result.tokenUsage.totalTokens * session.modelData.tokenMultiplier).toInt()
        )
        session.releaseSession()
    }

    private fun buildReturnValue(
        event: SlashCommandInteractionEvent,
        request: GPTRequest,
        result: ParsedGPTResult,
        session: ChattingSession,
    ): String {
        val builder = StringBuilder()
        appendApiInfo(event, builder, request, result, session)
        if (!request.hideRequest) appendRequest(builder, request)
        appendResult(builder, result)
        return builder.toString()
    }

    private fun appendModel(event: SlashCommandInteractionEvent, builder: StringBuilder, request: GPTRequest) {
        builder.append("└ 모델: ${request.model}").appendNewLine()
        appendParameter(event, builder, request)
    }

    private fun appendParameter(
        event: SlashCommandInteractionEvent, builder: StringBuilder, request: GPTRequest
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
        if (request.presetId != null) {
            builder.append("  └ Preset applied : ${"${event.getOption("preset-user")?.asMentionable?.asMention?.plus(":") ?: ""}`${request.presetId}`"}")
                .appendNewLine()
        }
        if (request.hideRequest) {
            builder.append("  └ Prompt hidden").appendNewLine()
        }
    }

    private fun appendApiInfo(
        event: SlashCommandInteractionEvent,
        builder: StringBuilder,
        request: GPTRequest,
        result: ParsedGPTResult,
        session: ChattingSession,
    ) {
        builder.append("**API 상세**:").appendNewLine(1)
        appendModel(event, builder, request)
        val usage = result.tokenUsage.totalTokens * session.modelData.tokenMultiplier
        builder.append("└ API 소모: ${result.tokenUsage.totalTokens}토큰 ((${result.tokenUsage.promptTokens} + ${result.tokenUsage.completionTokens}) x ${session.modelData.tokenMultiplier} = ${usage}t)")
        if (request.model in priceInfo) {
            val token = result.tokenUsage.totalTokens.toDouble() / 1000.0
            val price = round(priceInfo[request.model]!! * token * 10000.0) / 10000.0
            builder.append(" ($${price}, 추산치 ${round(dollarToWonMultiplier * price * 1000) / 1000.0}원)")
        }
        builder.appendNewLine()
        builder.append("└ API 응답 시간: ${decimalFormat.format(System.currentTimeMillis() - request.timeStamp)}ms")
            .appendNewLine(2)
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

    override suspend fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        val presets = if (event.getOption("preset-user") != null) {
            storage.getPresetList(event.guild!!.idLong, event.getOption("preset-user")!!.asLong, false)
        } else {
            storage.getPresetList(event.guild!!.idLong, event.member!!.idLong, true)
        }
        event.replyChoiceStrings(presets.filter { it.startsWith(event.focusedOption.value) }).queue()
    }
}