package skywolf46.devain.commands.discord.gpt.sessions

import arrow.core.identity
import arrow.core.left
import arrow.core.right
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.koin.core.component.inject
import skywolf46.devain.config.BotConfig
import skywolf46.devain.data.storage.ChattingSessionStorage
import skywolf46.devain.data.storage.SessionModelStorage
import skywolf46.devain.data.storage.SessionTokenStorage
import skywolf46.devain.platform.discord.DiscordCommand
import java.awt.Color

class StartGPTSessionCommand : DiscordCommand() {
    private val chattingSessionStorage by inject<ChattingSessionStorage>()

    private val sessionTokenStorage by inject<SessionTokenStorage>()

    private val sessionModelStorage by inject<SessionModelStorage>()

    private val config by inject<BotConfig>()
    override fun createCommandInfo(): Pair<String, CommandData> {
        return "session-start" to
                Commands.slash("session-start", "GPT 세션을 시작합니다.")
                    .addOption(OptionType.STRING, "model", "세션에 사용될 GPT 모델입니다.", true, true)
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        event.deferEmbed { _, hook ->
            val session = chattingSessionStorage.acquireSession(event.guild!!.idLong, event.member!!.idLong)
            session.toEither { null }.onRight {
                if (!it.acquireSession()) {
                    return@deferEmbed "채팅 세션이 사용중에 있습니다.".left()
                }
            }
            sessionModelStorage.getModelData(event.getOption("model")!!.asString).toEither { null }.mapLeft {
                EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("GPT 세션 생성 실패")
                    .setDescription("해당 모델은 GPT 세션의 사용이 허용되지 않았습니다.")
                    .build()
            }.map { model ->
                if (!sessionTokenStorage.withdrawIfEnough(event.user.idLong, model.sessionCreatePrice.toInt())) {
                    return@deferEmbed EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("GPT 세션 생성 실패")
                        .setDescription("세션 토큰이 부족합니다.\n/session-info 명령어로 세션 토큰 정보를 확인하세요.")
                        .build().right()
                }
                chattingSessionStorage.resetSession(
                    event.guild!!.idLong,
                    event.member!!.idLong,
                    event.getOption("model")!!.asString
                )
                EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("GPT 세션 생성됨")
                    .setDescription("${model.sessionCreatePrice} 세션 토큰을 사용하여 새로운 GPT 세션을 생성하였습니다.")
                    .addField("모델", "${event.getOption("model")!!.asString} (토큰 배수 x${model.tokenMultiplier})", false)
                    .addField("최대 보관 프롬프트", "${config.maxDialogCache} 메시지", false)
                    .build()
            }.fold(::identity, ::identity).right()
        }
    }


    override suspend fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        event.replyChoiceStrings(listOf("gpt-3.5-turbo", "gpt-4").filter { it.startsWith(event.focusedOption.value) })
            .queue()
    }
}