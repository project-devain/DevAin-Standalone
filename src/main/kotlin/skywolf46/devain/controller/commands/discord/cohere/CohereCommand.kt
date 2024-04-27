package skywolf46.devain.controller.commands.discord.cohere

import org.koin.core.component.inject
import skywolf46.devain.controller.api.requests.cohere.CohereGenerationAPICall
import skywolf46.devain.model.api.cohere.CohereGenerationRequest
import skywolf46.devain.platform.discord.AnnotatedParameterDiscordCommand
import skywolf46.devain.util.GenerationResultTextBuilder
import skywolf46.devain.util.TimeUtil

class CohereCommand :
    AnnotatedParameterDiscordCommand<CohereGenerationRequest>("cohere", CohereGenerationRequest::class) {
    private val apiCall by inject<CohereGenerationAPICall>()

    override suspend fun onParameterCommand(event: CommandEvent, data: CohereGenerationRequest) {
        event.origin.defer { _, hook ->
            apiCall.call(data.copy(model = "command")).fold({
                hook.sendMessage("오류가 발생했습니다. ${it.getErrorMessage()}").queue()
            }) {
                val builder = GenerationResultTextBuilder(
                    "Request Complete - Cohere Command", data.prompt, listOf(
                        mapOf(
                            "Model" to box("Command (Cohere)"),
                            "Elapsed" to box(TimeUtil.toTimeString(event.elapsed())),
                        ), mapOf("Response" to it.generations[0].text)
                    )
                )
                hook.sendMessageOrEmbed(1000, it.generations[0].text) { embed ->
                    builder.asEmbed(embed)
                }
            }
        }
    }

    override fun onCommandParameterDataRequested(): Map<String, String> {
        return mapOf("model" to "Cohere Command")
    }
}