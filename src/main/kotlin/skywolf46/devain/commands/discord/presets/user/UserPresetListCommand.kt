package skywolf46.devain.commands.discord.presets.user

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import skywolf46.devain.data.storage.PresetStorage
import skywolf46.devain.discord.DiscordCommand
import java.awt.Color

class UserPresetListCommand(private val storage: PresetStorage) : DiscordCommand() {
    override fun createCommandInfo(): Pair<String, CommandData> {
        return "preset-fetch" to Commands.slash("preset-fetch", "사용자의 프리셋을 조회합니다.")
            .addOption(OptionType.MENTIONABLE, "user", "대상 유저의 프리셋을 조회합니다. 관리자 권한이 필요합니다.", false)
            .addOption(OptionType.STRING, "preset", "대상 프리셋의 정보를 조회합니다. 지정되지 않았을 경우 자신을 대상으로 합니다.")
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        event.deferEmbed { _, hook ->
            executeCommand(event, hook)
        }
    }

    private suspend fun executeCommand(
        event: SlashCommandInteractionEvent,
        hook: InteractionHook
    ): Either<String, MessageEmbed> {
        val target = if (event.getOption("user") != null) {
            if (!event.member!!.isOwner && !event.member!!.roles.any { it.hasPermission(Permission.MANAGE_SERVER) }) {
                return "사용자 프리셋 조회는 관리자 권한으로만 사용이 가능합니다.".left()
            }
            event.getOption("user")!!.asMember ?: return "대상 멘션이 사용자가 아니거나 알 수 없는 사용자입니다.".left()
        } else {
            event.member ?: return "알 수 없는 사용자가 명령어를 사용하였습니다.".left()
        }
        return fetchPresets(target, event.getOption("user") == null)
    }

    private fun fetchPresets(member: Member, isSelf: Boolean): Either<String, MessageEmbed> {
        val presets = storage.getPresets(member.guild.idLong, member.idLong)
        if (presets.isEmpty()) {
            return if (isSelf) {
                "저장한 프리셋이 없습니다. \n/preset-add 명령어를 통해 프리셋을 추가할 수 있습니다.".left()
            } else {
                "대상 사용자가 지정한 프리셋이 존재하지 않습니다.".left()
            }
        }
        return EmbedBuilder().apply {
            setColor(Color.CYAN)
            setTitle("프리셋 목록")
            setDescription("${member.user.asMention}님의 프리셋 목록입니다.")
            for ((name, contents) in presets) {
                addField(name, if (contents.isShared) "공개됨" else "개인 프리셋으로 지정됨", false)
            }
        }.build().right()
    }


}