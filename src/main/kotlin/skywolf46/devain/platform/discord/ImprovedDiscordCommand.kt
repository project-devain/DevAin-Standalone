package skywolf46.devain.platform.discord

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.newFixedThreadPoolContext
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

abstract class ImprovedDiscordCommand(val command: String, val descrption: String = "제공된 명령어 설명이 존재하지 않습니다.") :
    DiscordCommand() {
    companion object {
        @OptIn(DelicateCoroutinesApi::class)
        private val devainCommandDispatcher = newFixedThreadPoolContext(10, "DevAin-Command Dispatcher")
    }

    private val completions = mutableMapOf<String, (CommandAutoCompleteInteraction) -> List<String>>()

    protected open fun modifyCommandData(options: SlashCommandData) {
        // Do nothing
    }

    protected fun SlashCommandData.addCompletableOption(
        name: String,
        description: String,
        required: Boolean,
        completion: (CommandAutoCompleteInteraction) -> List<String>
    ) : SlashCommandData {
        addOption(OptionType.STRING, name, description, required, true)
        completions[name] = completion
        return this
    }


    override suspend fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        println("Completion!")
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

}