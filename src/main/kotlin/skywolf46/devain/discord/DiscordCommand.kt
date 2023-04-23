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

    protected open suspend fun onCommand(event: SlashCommandInteractionEvent) {

    }

    protected open suspend fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {

    }

    abstract fun createCommandInfo(): Pair<String, CommandData>

    fun SlashCommandInteractionEvent.deferEmbed(unit: suspend (event: SlashCommandInteractionEvent, hook: InteractionHook) -> Either<String, MessageEmbed>) {
        deferReply().queue { hook ->
            CoroutineScope(devainCommandDispatcher).launch(Dispatchers.Main) {
                unit(this@deferEmbed, hook).onLeft {
                    hook.sendMessage(it).queue()
                }.onRight {
                    replyEmbeds(it).queue()
                }
            }
        }
    }


    suspend fun SlashCommandInteractionEvent.deferMessage(unit: suspend (event: SlashCommandInteractionEvent, hook: InteractionHook) -> Either<String, String>) {
        deferReply().queue { hook ->
            CoroutineScope(devainCommandDispatcher).launch {
                reply(unit(this@deferMessage, hook).onLeft {
                    hook.sendMessage(it).queue()
                }.fold(::identity, ::identity)).queue()
            }
        }
    }

    suspend fun SlashCommandInteractionEvent.deferError(unit: suspend (event: SlashCommandInteractionEvent, hook: InteractionHook) -> Either<String, Unit>) {
        deferReply().queue { hook ->
            CoroutineScope(devainCommandDispatcher).launch {
                unit(this@deferError, hook).onLeft {
                    hook.sendMessage(it).queue()
                }
            }
        }
    }

    suspend fun SlashCommandInteractionEvent.defer(unit: suspend (event: SlashCommandInteractionEvent, hook: InteractionHook) -> Unit) {
        deferReply().queue { hook ->
            CoroutineScope(devainCommandDispatcher).launch {
                unit(this@defer, hook)
            }
        }
    }
}