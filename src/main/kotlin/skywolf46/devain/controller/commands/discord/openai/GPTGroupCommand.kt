package skywolf46.devain.controller.commands.discord.openai

import arrow.core.getOrElse
import arrow.core.toOption
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import org.koin.core.component.get
import skywolf46.devain.model.api.openai.completion.OpenAIGPTMessage
import skywolf46.devain.model.store.OpenAIGPTGroupStore
import skywolf46.devain.model.store.OpenAIWorldStore
import skywolf46.devain.platform.discord.ImprovedDiscordCommand
import java.awt.Color

class GPTGroupCommand : ImprovedDiscordCommand("gpt-group", "새로운 GPT 그룹을 생성합니다. ") {
    private val groupStore = get<OpenAIGPTGroupStore>()
    private val worldStore = get<OpenAIWorldStore>()

    override fun modifyCommandData(options: SlashCommandData) {
        options
            .addSubcommandGroups(
                SubcommandGroupData("group", "GPT 그룹을 관리합니다.").addSubcommands(
                    SubcommandData("create", "새로운 GPT 그룹을 생성합니다.")
                        .addOption(OptionType.STRING, "name", "생성할 GPT 그룹 이름입니다.", true),
                    SubcommandData("info", "GPT 그룹 목록을 조회합니다.")
                        .addOption(OptionType.STRING, "name", "생성할 GPT 그룹 이름입니다.", true),
                    SubcommandData("turn-end", "턴 종료 프롬프트를 추가합니다.")
                        .addOption(OptionType.STRING, "name", "생성할 GPT 그룹 이름입니다.", true)
                        .addOption(OptionType.STRING, "prompt", "턴 종료 프롬프트입니다.", true),
                    SubcommandData("game-end", "게임 종료 프롬프트를 추가합니다.")
                        .addOption(OptionType.STRING, "name", "생성할 GPT 그룹 이름입니다.", true)
                        .addOption(OptionType.STRING, "prompt", "게임 종료 프롬프트입니다.", true),
                    SubcommandData("base-prompt", "기반 프롬프트를 추가합니다.")
                        .addOption(OptionType.STRING, "name", "생성할 GPT 그룹 이름입니다.", true)
                        .addOption(OptionType.STRING, "prompt", "기반 프롬프트입니다.", true),
                )
            ).addSubcommandGroups(
                SubcommandGroupData("personality", "GPT 인스턴스의 인격을 관리합니다.")
                    .addSubcommands(
//                        SubcommandData("create", "새 GPT 인스턴스를 생성합니다.")
//                            .addOption(OptionType.STRING, "name", "GPT 인스턴스의 이름입니다.", true),

                        SubcommandData("add", "등록된 GPT 인스턴스의 인격 지침을 추가합니다. GPT는 등록된 지침을 자신이라고 인식할것입니다.")
                            .addOption(OptionType.STRING, "group", "GPT 그룹의 이름입니다.", true)
                            .addOption(OptionType.STRING, "name", "GPT 인스턴스의 이름입니다.", true)
                            .addOption(OptionType.STRING, "prompt", "GPT 인스턴스의 인격 지침입니다.", true),
                    )
            )
            .addSubcommandGroups(
                SubcommandGroupData("world", "GPT 그룹이 실행되고 있는 세계를 관리합니다.")
                    .addSubcommands(
                        SubcommandData("create", "GPT 그룹을 사용하여 새 세계를 추가합니다.")
                            .addOption(OptionType.STRING, "name", "세계관의 이름입니다.", true)
                            .addOption(OptionType.STRING, "group", "사용할 GPT 그룹의 이름입니다.", true),
                        SubcommandData("join", "세계에 참가합니다. 이 명령은 지정된 세계가 시작되기 전에만 작동합니다.")
                            .addOption(OptionType.STRING, "name", "세계의 이름입니다.", true)
                            .addOption(OptionType.STRING, "nickname", "당신의 이름입니다.", true),
                        SubcommandData("start", "세계를 시작합니다.")
                            .addOption(OptionType.STRING, "name", "세계의 이름입니다.", true),
                        SubcommandData("step", "다음 단계를 진행합니다.")
                            .addOption(OptionType.STRING, "name", "세계의 이름입니다.", true),
                        SubcommandData("submit", "자신의 단계를 진행합니다.")
                            .addOption(OptionType.STRING, "name", "세계의 이름입니다.", true)
                            .addOption(OptionType.STRING, "prompt", "GPT에 전달할 프롬프트입니다.", true),
                    )
            )
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        when (event.subcommandGroup!!) {
            "group" -> onGroupCommand(event)
            "personality" -> onPersonalityCommand(event)
            "world" -> onWorldCommand(event)
        }
    }

    private fun onGroupCommand(event: SlashCommandInteractionEvent) {
        when (event.subcommandName) {
            "create" -> onCreateGroupCommand(event)
            "info" -> onGroupInfoCommand(event)
            "turn-end" -> onGroupTurnPromptCommand(event)
            "game-end" -> onGameTurnPromptCommand(event)
            "base-prompt" -> onAddBasePrompt(event)
        }
    }

    private fun onPersonalityCommand(event: SlashCommandInteractionEvent) {
        when (event.subcommandName) {
            "add" -> onAddPersonalityPrompt(event)
        }
    }

    private suspend fun onWorldCommand(event: SlashCommandInteractionEvent) {
        when (event.subcommandName) {
            "create" -> onCreateWorldCommand(event)
            "join" -> onJoinWorldCommand(event)
//            "start" -> onStartWorldCommand(event)
            "step" -> onStepWorldCommand(event)
            "submit" -> onSubmitWorldCommand(event)
        }
    }

    private suspend fun onSubmitWorldCommand(event: SlashCommandInteractionEvent) {
        worldStore.getWorld(event.getOption("name")!!.asString).getOrElse {
            event.replyEmbeds(
                EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("세계 진행 실패")
                    .setDescription("세계 ${event.getOption("name")!!.asString}은(는) 등록된 세계가 아닙니다.")
                    .build()
            ).queue()
            return
        }.playerNextStep(event)
    }

    private fun onCreateWorldCommand(event: SlashCommandInteractionEvent) {
        val group = groupStore.getGroup(event.getOption("group")!!.asString).getOrElse {
            event.replyEmbeds(
                EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("세계 생성 실패")
                    .setDescription("GPT 그룹 ${event.getOption("group")!!.asString}은(는) 등록된 그룹이 아닙니다.")
                    .build()
            ).queue()
            return
        }
        if (worldStore.createWorld(event.getOption("name")!!.asString, group)) {
            event.replyEmbeds(
                EmbedBuilder()
                    .setColor(Color.CYAN)
                    .setTitle("세계 생성 성공")
                    .setDescription("세계 ${event.getOption("name")!!.asString}을(를) 생성하였습니다.")
                    .build()
            ).queue()
        } else {
            event.replyEmbeds(
                EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("세계 생성 실패")
                    .setDescription("세계 ${event.getOption("name")!!.asString}은(는) 이미 등록된 세계입니다.")
                    .build()
            ).queue()
        }
    }

    private suspend fun onStepWorldCommand(event: SlashCommandInteractionEvent) {
        worldStore.getWorld(event.getOption("name")!!.asString).getOrElse {
            event.replyEmbeds(
                EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("세계 진행 실패")
                    .setDescription("세계 ${event.getOption("name")!!.asString}은(는) 등록된 세계가 아닙니다.")
                    .build()
            ).queue()
            return
        }.nextStep(event)
    }

    private fun onJoinWorldCommand(event: SlashCommandInteractionEvent) {
        val world = worldStore.getWorld(event.getOption("name")!!.asString).getOrElse {
            event.replyEmbeds(
                EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("세계 참가 실패")
                    .setDescription("세계 ${event.getOption("name")!!.asString}은(는) 등록된 세계가 아닙니다.")
                    .build()
            ).queue()
            return
        }
        world.joinPlayer(event.user.idLong, event.getOption("nickname")!!.asString)
    }


    private fun onAddPersonalityPrompt(event: SlashCommandInteractionEvent) {
        val group = groupStore.getGroup(event.getOption("group")!!.asString).getOrElse {
            event.replyEmbeds(
                EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("AI 인격 생성 실패")
                    .setDescription("GPT 그룹 ${event.getOption("group")!!.asString}은(는) 이미 등록된 그룹입니다.")
                    .build()
            ).queue()
            return
        }
        val personality = group.getOrCreatePersonality(event.getOption("name")!!.asString)
        personality.personality.add(
            OpenAIGPTMessage(
                OpenAIGPTMessage.Role.USER_PRECONDITION,
                event.getOption("prompt")!!.asString.toOption()
            )
        )
        event.replyEmbeds(
            EmbedBuilder()
                .setColor(Color.CYAN)
                .setTitle("AI 인격 프롬프트 추가 성공")
                .setDescription("GPT 그룹 ${event.getOption("name")!!.asString}의 인격 ${event.getOption("name")!!.asString}에 새 프롬프트를 추가하였습니다.")
                .build()
        ).queue()
    }

    private fun onCreateGroupCommand(event: SlashCommandInteractionEvent) {
        event.replyEmbeds(
            if (groupStore.createGroup(event.getOption("name")!!.asString))
                EmbedBuilder()
                    .setColor(Color.CYAN)
                    .setTitle("GPT 그룹 생성 성공")
                    .setDescription("GPT 그룹 ${event.getOption("name")!!.asString}을(를) 생성하였습니다.")
                    .build()
            else
                EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("GPT 그룹 생성 실패")
                    .setDescription("GPT 그룹 ${event.getOption("name")!!.asString}은(는) 이미 등록된 그룹입니다.")
                    .build()
        ).queue()

    }

    private fun onGroupInfoCommand(event: SlashCommandInteractionEvent) {
        groupStore.getGroup(event.getOption("name")!!.asString).tap {
            event.replyEmbeds(
                EmbedBuilder()
                    .setColor(Color.LIGHT_GRAY)
                    .setTitle("GPT 그룹 정보")
                    .setDescription("## ${event.getOption("name")!!.asString}")
                    .addField("회상", box("${it.allowedHistorySize} 프롬프트"), true)
                    .addField("기반 프롬프트", box("${it.getBasePrompts().size}개"), true)
                    .addField("턴 종료 프롬프트", box("${it.getTurnCheckTokens().size}개"), true)
                    .addField("인스턴스 (${it.getInstances().size})",
                        box(it.getInstances().map { instance -> "- ${instance.gptName}" }
                            .joinToString("\n")),
                        false
                    )
                    .build()
            ).queue()
        }.tapNone {
            event.replyEmbeds(
                EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("GPT 그룹 조회 실패")
                    .setDescription("GPT 그룹 ${event.getOption("name")!!.asString}은(는) 등록된 그룹이 아닙니다.")
                    .build()
            ).queue()
        }
    }


    private fun onGroupTurnPromptCommand(event: SlashCommandInteractionEvent) {
        groupStore.getGroup(event.getOption("name")!!.asString).tap {
            it.addTurnCheckToken(
                OpenAIGPTMessage(
                    OpenAIGPTMessage.Role.USER_PRECONDITION,
                    event.getOption("prompt")!!.asString.toOption()
                )
            )
            event.replyEmbeds(
                EmbedBuilder()
                    .setColor(Color.CYAN)
                    .setTitle("기반 프롬프트 추가 성공")
                    .setDescription("GPT 그룹 ${event.getOption("name")!!.asString}에 새 턴 종료 프롬프트를 추가하였습니다.")
                    .build()
            ).queue()
        }.tapNone {
            event.replyEmbeds(
                EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("GPT 그룹 조회 실패")
                    .setDescription("GPT 그룹 ${event.getOption("name")!!.asString}은(는) 등록된 그룹이 아닙니다.")
                    .build()
            ).queue()
        }
    }

    private fun onGameTurnPromptCommand(event: SlashCommandInteractionEvent) {
        groupStore.getGroup(event.getOption("name")!!.asString).tap {
            it.addGameEndCheckToken(
                OpenAIGPTMessage(
                    OpenAIGPTMessage.Role.USER_PRECONDITION,
                    event.getOption("prompt")!!.asString.toOption()
                )
            )
            event.replyEmbeds(
                EmbedBuilder()
                    .setColor(Color.CYAN)
                    .setTitle("기반 프롬프트 추가 성공")
                    .setDescription("GPT 그룹 ${event.getOption("name")!!.asString}에 새 게임 종료 프롬프트를 추가하였습니다.")
                    .build()
            ).queue()
        }.tapNone {
            event.replyEmbeds(
                EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("GPT 그룹 조회 실패")
                    .setDescription("GPT 그룹 ${event.getOption("name")!!.asString}은(는) 등록된 그룹이 아닙니다.")
                    .build()
            ).queue()
        }
    }


    private fun onAddBasePrompt(event: SlashCommandInteractionEvent) {
        groupStore.getGroup(event.getOption("name")!!.asString).tap {
            it.addBasePrompt(
                OpenAIGPTMessage(
                    OpenAIGPTMessage.Role.USER_PRECONDITION,
                    event.getOption("prompt")!!.asString.toOption()
                )
            )
            event.replyEmbeds(
                EmbedBuilder()
                    .setColor(Color.CYAN)
                    .setTitle("기반 프롬프트 추가 성공")
                    .setDescription("GPT 그룹 ${event.getOption("name")!!.asString}에 새 기반 프롬프트를 추가하였습니다.")
                    .build()
            ).queue()
        }.tapNone {
            event.replyEmbeds(
                EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("GPT 그룹 조회 실패")
                    .setDescription("GPT 그룹 ${event.getOption("name")!!.asString}은(는) 등록된 그룹이 아닙니다.")
                    .build()
            ).queue()
        }
    }
}