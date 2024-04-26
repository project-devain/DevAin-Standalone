package skywolf46.devain.controller.commands.discord.cohere

import org.koin.core.component.inject
import skywolf46.devain.controller.api.requests.cohere.CommandRPlusAPICall
import skywolf46.devain.model.api.rplus.RPlusRequest
import skywolf46.devain.platform.discord.AnnotatedParameterDiscordCommand
import skywolf46.devain.util.GenerationResultTextBuilder
import skywolf46.devain.util.TimeUtil

class CommandRPlusCommand : AnnotatedParameterDiscordCommand<RPlusRequest>("rplus", RPlusRequest::class) {
    private val apiCall by inject<CommandRPlusAPICall>()

    override suspend fun onParameterCommand(event: CommandEvent, data: RPlusRequest) {
        event.origin.defer { _, hook ->
            apiCall.call(data.copy(model = "command-r-plus")).fold({
                hook.sendMessage("오류가 발생했습니다. ${it.getErrorMessage()}").queue()
            }) {
                val builder = GenerationResultTextBuilder("Request Complete - Command R+", data.message, listOf(
                    mapOf(
                        "Model" to box("Command R+ (Cohere)"),
                        "Elapsed" to box(TimeUtil.toTimeString(event.elapsed())),
                    ),
                    mapOf("Response" to it.text)
                ))
                hook.sendMessageOrEmbed(1000, it.text) { embed ->
                    builder.asEmbed(embed)
                }
            }
        }
    }

    override fun onCommandParameterDataRequested(): Map<String, String> {
        return mapOf("model" to "Command R+")
    }
}
