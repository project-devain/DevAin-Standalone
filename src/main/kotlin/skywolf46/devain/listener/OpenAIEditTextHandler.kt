package skywolf46.devain.listener

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import skywolf46.devain.config.BotConfig
import skywolf46.devain.util.OpenAiRequest

object OpenAIEditTextHandler {
    fun handle(config: BotConfig, model: String, event: SlashCommandInteractionEvent) {
        event.deferReply(false).queue { hook ->
            processRequest(hook, model, config, event.getOption("input")!!.asString, event.getOption("instruction")!!.asString)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun processRequest(hook: InteractionHook, model: String, config: BotConfig, message: String, instruction: String) {
        GlobalScope.launch {
            kotlin.runCatching {
                val result = OpenAiRequest.requestEdit(config.openAIToken, model, message, instruction)
                result.onLeft {
                    hook.sendMessage("OpenAI API가 오류를 반환하였습니다 : $it").queue()
                }.onRight {
                    hook.sendMessage("**모델**: ${model}\n\n**요청**: ${instruction}\n\n**원본**:\n${message}\n\n**수정된 텍스트**: \n${it.choices[0].result}").queue()
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }
}