package skywolf46.devain.model.api.openai

import arrow.core.*
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import skywolf46.devain.controller.api.APIError
import skywolf46.devain.controller.api.error.UnexpectedError
import skywolf46.devain.controller.api.requests.openai.GPTCompletionAPICall
import skywolf46.devain.model.api.openai.completion.OpenAIGPTMessage
import skywolf46.devain.model.api.openai.completion.OpenAIGPTGroup
import skywolf46.devain.model.api.openai.completion.OpenAIGPTPersonality
import skywolf46.devain.model.api.openai.completion.OpenAIGPTRequest
import java.util.concurrent.atomic.AtomicInteger

data class OpenAIWorld(val group: OpenAIGPTGroup) : KoinComponent {
    private val prompts = mutableListOf<OpenAIWorldLog>()

    private val currentIndex = AtomicInteger(0)

    private val apiCall by inject<GPTCompletionAPICall>()

    private var turn = 0

    private val personality = mutableListOf<Personality>().apply {
        addAll(group.getInstances().map { AIPersonality(it) })
    }

    fun buildPrompts(): MutableList<OpenAIGPTMessage> {
        val currentPersonality = personality[currentIndex.get()]
        val promptBuilt = mutableListOf<OpenAIGPTMessage>()
        promptBuilt.addAll(group.getBasePrompts())
        promptBuilt.addAll(
            prompts.map {
                if (it.from.getName() == currentPersonality.getName())
                    OpenAIGPTMessage(
                        OpenAIGPTMessage.Role.USER_PRECONDITION,
                        "${it.message.content.orNull()}".toOption()
                    )
                else
                    OpenAIGPTMessage(
                        OpenAIGPTMessage.Role.ASSISTANT,
                        "${it.from.getName()}: ${it.message.content.orNull()}".toOption()
                    )
            })
        promptBuilt.addAll((currentPersonality as AIPersonality).personality.personality)
        promptBuilt.add(
            OpenAIGPTMessage(
                OpenAIGPTMessage.Role.USER,
                "당신은 ${currentPersonality.personality.gptName}입니다. 당신이 말하고 싶은 내용만 말하십시오. 다른 사용자를 언급하는것은 해당되나, 해당 사람처럼 연기하는것은 허용되지 않습니다.".toOption()
            )
        )
        promptBuilt.addAll(group.getTurnCheckTokens().map {
            OpenAIGPTMessage(
                OpenAIGPTMessage.Role.USER_PRECONDITION,
                "${it.content.orNull()}".replace(
                    "{turn}", (group.turn - turn).toString()
                ).toOption()
            )
        })
        return promptBuilt
    }

    fun buildEndPrompts(gptName: String): MutableList<OpenAIGPTMessage> {
        val currentPersonality = personality[currentIndex.get()]
        val promptBuilt = mutableListOf<OpenAIGPTMessage>()
        promptBuilt.addAll(group.getBasePrompts())
        promptBuilt.addAll(
            prompts.map {
                if (it.from.getName() == currentPersonality.getName())
                    OpenAIGPTMessage(
                        OpenAIGPTMessage.Role.USER_PRECONDITION,
                        "${it.message.content.orNull()}".toOption()
                    )
                else
                    OpenAIGPTMessage(
                        OpenAIGPTMessage.Role.ASSISTANT,
                        "${it.from.getName()}: ${it.message.content.orNull()}".toOption()
                    )
            })
        promptBuilt.addAll((currentPersonality as AIPersonality).personality.personality)
        promptBuilt.add(
            OpenAIGPTMessage(
                OpenAIGPTMessage.Role.USER,
                "당신은 ${gptName}입니다.".toOption()
            )
        )

        promptBuilt.addAll(group.getGameEndTokens().map {
            OpenAIGPTMessage(
                OpenAIGPTMessage.Role.USER_PRECONDITION,
                "${it.content.orNull()}".replace(
                    "{turn}", (group.turn - turn).toString()
                ).toOption()
            )
        })
        return promptBuilt
    }

    suspend fun nextStep(event: SlashCommandInteractionEvent) {
        event.deferReply().queue { hook ->
            runBlocking {
                if (turn == group.turn) {
                    hook.sendMessageEmbeds(
                        EmbedBuilder().setTitle("게임 종료").setDescription("게임이 종료되었습니다. 게임을 집계합니다..").build()
                    ).queue {
                        runBlocking {
                            val list = personality.associateWith { it.requestEnd(event, hook, turn) }
                            val builder = EmbedBuilder().setTitle("게임 결과")
                            list.forEach { (t, u) ->
                                builder.addField(
                                    "${t.getName()}의 의견",
                                    u.map { it.content.orNull()!! }.getOrElse { "오류가 발생했습니다." },
                                    false
                                )
                            }
                            it.editMessageEmbeds(builder.build()).queue()
                        }
                    }
                    return@runBlocking
                } else if (turn >= group.turn) {
                    hook.sendMessage("이미 게임이 종료되었습니다.").queue()
                    return@runBlocking
                }
                val currentPersonality = personality[currentIndex.get()]
                val message = currentPersonality.requestMessage(event, hook, ++turn).getOrElse {
                    hook.sendMessage(it.getErrorMessage()).queue()
                    return@runBlocking
                }
                addPromptLog(message)
                currentPersonality.replyAfter(hook)
            }
        }
    }


    suspend fun playerNextStep(event: SlashCommandInteractionEvent) {
        event.deferReply().queue { hook ->
            runBlocking {
                val currentPersonality = personality[currentIndex.get()]
                if (currentPersonality !is PlayerPersonality || currentPersonality.idLong != event.user.idLong) {
                    hook.sendMessage("당신의 차례가 아닙니다.").queue()
                    return@runBlocking
                }
                val message = currentPersonality.requestMessage(event, hook, ++turn).getOrElse {
                    hook.sendMessage(it.getErrorMessage()).queue()
                    return@runBlocking
                }
                addPromptLog(message)
                currentPersonality.replyAfter(hook)
            }
        }
    }

    fun addPromptLog(prompt: OpenAIGPTMessage) {
        val currentPersonality = personality[currentIndex.get()]
        prompts.add(OpenAIWorldLog(currentPersonality, prompt))
        if (prompts.size > group.allowedHistorySize) prompts.removeAt(0)
        checkIndex()
    }


    fun checkIndex() {
        if (currentIndex.incrementAndGet() >= group.getInstances().size) {
            currentIndex.set(0)
        }
    }

    fun joinPlayer(idLong: Long, name: String) {
        personality.add(PlayerPersonality(idLong, name))
    }

    interface Personality : KoinComponent {
        suspend fun requestMessage(
            event: SlashCommandInteractionEvent, hook: InteractionHook, turn: Int
        ): Either<APIError, OpenAIGPTMessage>

        suspend fun requestEnd(
            event: SlashCommandInteractionEvent, hook: InteractionHook, turn: Int
        ): Either<APIError, OpenAIGPTMessage>

        suspend fun replyAfter(hook: InteractionHook)

        fun getName(): String
    }

    inner class AIPersonality(val personality: OpenAIGPTPersonality) : Personality {
        override suspend fun requestMessage(
            event: SlashCommandInteractionEvent, hook: InteractionHook, turn: Int
        ): Either<APIError, OpenAIGPTMessage> {
            return apiCall.call(
                OpenAIGPTRequest(
                    "gpt-4-0613", buildPrompts(), 1, maxTokens = 60.toOption()
                )
            ).map {
                OpenAIGPTMessage(
                    OpenAIGPTMessage.Role.USER, "${it.answers[0].message.content.orNull()}".toOption()
                )
            }.onRight {
                hook.sendMessageEmbeds(
                    EmbedBuilder()
                        .setTitle(personality.gptName)
                        .setDescription(it.content.orNull().toString())
                        .addField("세계관 턴", turn.toString(), false)
                        .build()
                ).queue()
            }
        }

        override suspend fun requestEnd(
            event: SlashCommandInteractionEvent, hook: InteractionHook, turn: Int
        ): Either<APIError, OpenAIGPTMessage> {
            return apiCall.call(
                OpenAIGPTRequest(
                    "gpt-4-0613", buildEndPrompts(personality.gptName), 1, maxTokens = 60.toOption()
                )
            ).map {
                OpenAIGPTMessage(
                    OpenAIGPTMessage.Role.USER, "${it.answers[0].message.content.orNull()}".toOption()
                )
            }
        }

        override suspend fun replyAfter(hook: InteractionHook) {

        }

        override fun getName(): String {
            return personality.gptName
        }
    }

    inner class PlayerPersonality(val idLong: Long, private val name: String) : Personality {
        override suspend fun requestMessage(
            event: SlashCommandInteractionEvent, hook: InteractionHook, turn: Int
        ): Either<APIError, OpenAIGPTMessage> {
            if (event.user.idLong != idLong) return UnexpectedError(RuntimeException("당신의 차례가 아닙니다.")).left()
            hook.sendMessage(event.getOption("prompt")!!.asString).queue()
            return OpenAIGPTMessage(
                OpenAIGPTMessage.Role.USER, event.getOption("prompt")!!.asString.toOption()
            ).right()
        }

        override suspend fun requestEnd(
            event: SlashCommandInteractionEvent,
            hook: InteractionHook,
            turn: Int
        ): Either<APIError, OpenAIGPTMessage> {
            TODO("Not yet implemented")
        }

        override suspend fun replyAfter(hook: InteractionHook) {
            hook.sendMessage("<@${idLong}>님의 차례입니다.").queue()
            return
        }

        override fun getName(): String {
            return name
        }

    }

    data class OpenAIWorldLog(val from: Personality, val message: OpenAIGPTMessage) {
        fun asPrompt(currentPersonality: Personality): OpenAIGPTMessage {
            return if (from == currentPersonality) {
                OpenAIGPTMessage(OpenAIGPTMessage.Role.ASSISTANT, message.content)
            } else {
                OpenAIGPTMessage(OpenAIGPTMessage.Role.USER, message.content)
            }
        }
    }

}