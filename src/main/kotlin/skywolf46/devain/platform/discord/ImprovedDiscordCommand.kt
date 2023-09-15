package skywolf46.devain.platform.discord

import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.newFixedThreadPoolContext
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.modals.Modal
import java.lang.IllegalStateException

abstract class ImprovedDiscordCommand(
    val command: String,
    val descrption: String = "제공된 명령어 설명이 존재하지 않습니다.",
    val modalId: Option<String> = None
) :
    DiscordCommand() {
    companion object {
        @OptIn(DelicateCoroutinesApi::class)
        private val devainCommandDispatcher = newFixedThreadPoolContext(10, "DevAin-Command Dispatcher")
    }

    private val userModalHandler = mutableMapOf<Long, (ModalInteractionEvent) -> Unit>()

    private val completions = mutableMapOf<String, (CommandAutoCompleteInteraction) -> List<String>>()


    fun createModal(modalTitle: String, builder: (Modal.Builder) -> Unit): Modal {
        return modalId.getOrElse {
            throw IllegalStateException("이 명령어에는 모달 ID가 등록되지 않았습니다. 생성자에서 모달 ID를 지정하세요.")
        }.let {
            Modal.create(it, modalTitle).apply(builder)
        }.build()
    }

    protected fun SlashCommandInteraction.listenModal(modal: Modal, onModal: (ModalInteractionEvent) -> Unit) {
        userModalHandler[user.idLong] = onModal
        replyModal(modal).queue()
    }

    protected open fun modifyCommandData(options: SlashCommandData) {
        // Do nothing
    }

    open fun onModal(event: ModalInteractionEvent) {
        userModalHandler.remove(event.user.idLong)?.invoke(event)
    }

    protected fun SlashCommandData.addCompletableOption(
        name: String,
        description: String,
        required: Boolean,
        completion: (CommandAutoCompleteInteraction) -> List<String>
    ): SlashCommandData {
        addOption(OptionType.STRING, name, description, required, true)
        completions[name] = completion
        return this
    }


    override suspend fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        if (event.focusedOption.name in completions) {
            event.replyChoiceStrings(
                completions[event.focusedOption.name]!!.invoke(event)
                    .filter { it.startsWith(event.focusedOption.value) }
                    .let { it.subList(0, it.size.coerceAtMost(25)) })
                .queue()
        }
    }

    fun createCommandData(): SlashCommandData {
        return Commands.slash(command, descrption).apply {
            modifyCommandData(this)
        }
    }

    override fun createCommandInfo(): Pair<String, CommandData> {
        return command to createCommandData()
    }

    protected fun box(string: String) : String {
        return "```$string```"
    }

}