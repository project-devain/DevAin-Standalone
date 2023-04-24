package skywolf46.devain.discord

import arrow.core.Either
import arrow.core.identity
import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.build.CommandData

abstract class DiscordCommand {
    companion object {
        @OptIn(DelicateCoroutinesApi::class)
        private val devainCommandDispatcher = newFixedThreadPoolContext(10, "DevAin-Command Dispatcher")
    }

    fun triggerCommand(event: SlashCommandInteractionEvent) {
        runBlocking { onCommand(event) }
    }

    fun triggerAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        runBlocking { onAutoComplete(event) }
    }

    protected open suspend fun onCommand(event: SlashCommandInteractionEvent) {

    }

    protected open suspend fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {

    }

    abstract fun createCommandInfo(): Pair<String, CommandData>

    fun SlashCommandInteractionEvent.deferEmbed(
        isEphemeral: Boolean = false,
        unit: suspend (event: SlashCommandInteractionEvent, hook: InteractionHook) -> Either<String, MessageEmbed>
    ) {
        deferReply(isEphemeral).queue { hook ->
            CoroutineScope(devainCommandDispatcher).launch {
                unit(this@deferEmbed, hook).onLeft {
                    hook.sendMessage(it).queue()
                }.onRight {
                    hook.sendMessageEmbeds(it).queue()
                }
            }
        }
    }

    suspend fun SlashCommandInteractionEvent.deferMessage(
        isEphemeral: Boolean = false,
        unit: suspend (event: SlashCommandInteractionEvent, hook: InteractionHook) -> Either<String, String>
    ) {
        deferReply(isEphemeral).queue { hook ->
            CoroutineScope(devainCommandDispatcher).launch {
                hook.sendMessage(unit(this@deferMessage, hook).fold(::identity, ::identity)).queue()
            }
        }
    }

    suspend fun SlashCommandInteractionEvent.deferError(
        isEphemeral: Boolean = false,
        unit: suspend (event: SlashCommandInteractionEvent, hook: InteractionHook) -> Either<String, Unit>
    ) {
        deferReply(isEphemeral).queue { hook ->
            CoroutineScope(devainCommandDispatcher).launch {
                unit(this@deferError, hook).onLeft {
                    hook.sendMessage(it).queue()
                }
            }
        }
    }

    suspend fun SlashCommandInteractionEvent.defer(
        isEphemeral: Boolean = false,
        unit: suspend (event: SlashCommandInteractionEvent, hook: InteractionHook) -> Unit
    ) {
        deferReply(isEphemeral).queue { hook ->
            CoroutineScope(devainCommandDispatcher).launch {
                unit(this@defer, hook)
            }
        }
    }
}