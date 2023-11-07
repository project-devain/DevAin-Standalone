package skywolf46.devain.model.api.openai.completion

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class OpenAIGPTGroup(val allowedHistorySize: Int, val turn: Int) {
    private val promptLock = ReentrantLock()

    private val basePrompts = mutableListOf<OpenAIGPTMessage>()

    private val gameEndPrompts = mutableListOf<OpenAIGPTMessage>()

    private val turnCheckTokens = mutableListOf<OpenAIGPTMessage>()

    private val groups = mutableListOf<OpenAIGPTPersonality>()



    fun addBasePrompt(message: OpenAIGPTMessage): OpenAIGPTGroup {
        promptLock.withLock {
            basePrompts += message
        }
        return this
    }

    fun addGPTPersonality(personality: OpenAIGPTPersonality): OpenAIGPTGroup {
        promptLock.withLock {
            groups += personality
        }
        return this
    }

    fun addTurnCheckToken(message: OpenAIGPTMessage): OpenAIGPTGroup {
        promptLock.withLock {
            turnCheckTokens += message
        }
        return this
    }

    fun addGameEndCheckToken(message: OpenAIGPTMessage): OpenAIGPTGroup {
        promptLock.withLock {
            gameEndPrompts += message
        }
        return this
    }



    fun getBasePrompts(): List<OpenAIGPTMessage> {
        return promptLock.withLock {
            basePrompts.toList()
        }
    }

    fun getTurnCheckTokens(): List<OpenAIGPTMessage> {
        return promptLock.withLock {
            turnCheckTokens.toList()
        }
    }


    fun getGameEndTokens(): List<OpenAIGPTMessage> {
        return promptLock.withLock {
            gameEndPrompts.toList()
        }
    }

    fun getInstances(): List<OpenAIGPTPersonality> {
        return promptLock.withLock {
            groups.toList()
        }
    }

    fun getOrCreatePersonality(name: String): OpenAIGPTPersonality {
        return promptLock.withLock {
            groups.firstOrNull { it.gptName == name } ?: OpenAIGPTPersonality(name, mutableListOf()).also {
                groups += it
            }
        }
    }

}