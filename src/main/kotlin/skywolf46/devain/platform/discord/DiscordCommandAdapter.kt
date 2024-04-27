package skywolf46.devain.platform.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class DiscordCommandAdapter(private val jda: JDA) : ListenerAdapter() {
    private val commandRegistry = mutableMapOf<String, DiscordCommand>()
    private val modalRegistry = mutableMapOf<String, ImprovedDiscordCommand>()

    fun registerCommands(vararg command: DiscordCommand) {
        val updateAction = jda.updateCommands()
        for (x in command) {
            val commandData = x.createCommandInfo()
            commandRegistry[commandData.first] = x
            updateAction.addCommands(commandData.second)
            if (x is ImprovedDiscordCommand) {
                x.modalId.onSome {
                    modalRegistry[it] = x
                }
            }
        }
        updateAction.queue()
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        commandRegistry[event.name]?.triggerCommand(event)
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        commandRegistry[event.name]?.triggerAutoComplete(event)
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        modalRegistry[event.interaction.modalId]?.onModal(event)
    }
}