package skywolf46.devain.commands.discord.presets.user

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.koin.core.component.inject
import skywolf46.devain.config.BotConfig
import skywolf46.devain.data.storage.PresetStorage
import skywolf46.devain.platform.discord.DiscordCommand

class UserPresetAddCommand : DiscordCommand() {
    private val config by inject<BotConfig>()
    private val storage by inject<PresetStorage>()

    override fun createCommandInfo(): Pair<String, CommandData> {
        return "preset-add" to Commands.slash("preset-add", "새 프리셋을 등록합니다.")
            .addOption(OptionType.STRING, "name", "프리셋 이름을 지정합니다.", true)
            .addOption(OptionType.STRING, "prompt", "프리셋 내용을 설정합니다.", true)
            .addOption(OptionType.BOOLEAN, "open-sourced", "이 프리셋을 공개 프리셋으로 설정할지의 여부입니다.", false)
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        event.deferMessage(true) { _, _ ->
            executeCommand(event)
        }
    }

    private fun executeCommand(
        event: SlashCommandInteractionEvent,
    ): Either<String, String> {
        checkRestriction(event).onLeft {
            return it.left()
        }
        return insertPreset(
            event.member!!,
            event.getOption("name")!!.asString,
            event.getOption("prompt")!!.asString,
            event.getOption("open-sourced")?.asBoolean ?: false
        )
    }

    private fun checkRestriction(event: SlashCommandInteractionEvent): Either<String, Unit> {
        if (event.getOption("name")!!.asString.isEmpty()) {
            return "프리셋 이름은 비어있는 문자열로 사용할 수 없습니다.".left()
        }
        if (event.getOption("name")!!.asString.length > 16) {
            return "프리셋 이름은 최대 12자까지 사용 가능합니다.".left()
        }
        if (event.getOption("prompt")!!.asString.isEmpty()) {
            return "프리셋 내용은 비어있는 문자열로 사용할 수 없습니다.".left()
        }
        if (event.getOption("prompt")!!.asString.length > 500) {
            return "프리셋 내용은 최대 500자까지만 사용 가능합니다.".left()
        }
        if (event.getOption("name")!!.asString.replace(Regex("[0-9a-zㄱ-ㅁ가-힣ㅏ-ㅣ ]"), "").isNotEmpty()) {
            return "프리셋 이름에는 특수 문자 사용이 불가능합니다.".left()
        }
        return Unit.right()
    }

    private fun insertPreset(member: Member, name: String, preset: String, isShared: Boolean): Either<String, String> {
        val defined = storage.getPresets(member.guild.idLong, member.idLong)
        if (defined.size > config.maxUserPresetPerServer && name !in defined) {
            return "유저 프리셋은 사용자당 최대 ${config.maxUserPresetPerServer}개까지 지정 가능합니다.".left()
        }
        storage.updatePreset(member.guild.idLong, member.idLong, name, preset, isShared)
        return if (name in defined) {
            "기존 프리셋 `$name`을 갱신하였습니다."
        } else {
            "새 프리셋 `$name`을 등록하였습니다."
        }.right()
    }


}