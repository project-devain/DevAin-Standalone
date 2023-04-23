package skywolf46.devain.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class DiscordCommandAdapter(private val jda: JDA) : ListenerAdapter() {
    private val commandRegistry = mutableMapOf<String, DiscordCommand>()

    fun registerCommands(vararg command: DiscordCommand) {
        val updateAction = jda.updateCommands()
        for (x in command) {
            val commandData = x.createCommandInfo()
            commandRegistry[commandData.first] = x
            updateAction.addCommands(commandData.second)
        }
        updateAction.queue()
    }


    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        commandRegistry[event.name]?.triggerCommand(event)
    }


}