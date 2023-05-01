package skywolf46.devain.commands.discord.gpt.sessions

import arrow.core.right
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.utils.TimeFormat
import org.koin.core.component.inject
import skywolf46.devain.config.BotConfig
import skywolf46.devain.data.parsed.gpt.sessions.SessionTokenData
import skywolf46.devain.data.storage.SessionTokenStorage
import skywolf46.devain.platform.discord.DiscordCommand
import java.awt.Color

class SessionInfoCommand : DiscordCommand() {
    val config by inject<BotConfig>()
    val sessionTokenStorage by inject<SessionTokenStorage>()

    override fun createCommandInfo(): Pair<String, CommandData> {
        return "session-info" to
                Commands.slash("session-info", "세션 정보를 조회합니다.")
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        event.deferEmbed { _, hook ->
            val data = sessionTokenStorage.estimateToken(event.user.idLong)
            EmbedBuilder().buildSessionInfoEmbed(data).build().right()
        }
    }

    private fun EmbedBuilder.buildSessionInfoEmbed(data: SessionTokenData): EmbedBuilder {
        setTitle("사용자 GPT 세션 정보")
        setColor(Color.LIGHT_GRAY)
        buildUserInfo(data)
        return this
    }

    private fun EmbedBuilder.buildUserInfo(data: SessionTokenData) {
        val percentage = (data.tokenAmount.toDouble() / config.maxSessionToken.toDouble() * 100.0).toInt()
        addField("잔여 세션 토큰", "${data.tokenAmount}t / ${config.maxSessionToken}t (${percentage}%)", false)
        if (data.isFullToken)
            addField("세션 토큰 최대 재생 시간", "0초", false)
        else
            addField("세션 토큰 최대 재생 시간", TimeFormat.RELATIVE.format(data.estimateRegenerateEndTime(config)), false)
        setFooter("세션 토큰은 GPT 모델의 소모량이 아닌, 세션의 사용량입니다.\n모델마다 세션 토큰의 소모량이 다릅니다.")
    }
}