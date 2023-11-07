package skywolf46.devain.model.store

import arrow.core.Option
import arrow.core.toOption
import skywolf46.devain.model.api.openai.OpenAIWorld
import skywolf46.devain.model.api.openai.completion.OpenAIGPTGroup

class OpenAIWorldStore {
    private val worlds = mutableMapOf<String, OpenAIWorld>()

    fun getWorld(name: String): Option<OpenAIWorld> {
        return worlds[name].toOption()
    }

    fun createWorld(name: String, group: OpenAIGPTGroup): Boolean {
        if (worlds.containsKey(name))
            return false
        worlds[name] = OpenAIWorld(group)
        return true
    }
}