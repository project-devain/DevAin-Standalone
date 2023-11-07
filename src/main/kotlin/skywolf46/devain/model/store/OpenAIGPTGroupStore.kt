package skywolf46.devain.model.store

import arrow.core.Option
import arrow.core.toOption
import skywolf46.devain.model.api.openai.completion.OpenAIGPTGroup

class OpenAIGPTGroupStore {
    private val store = mutableMapOf<String, OpenAIGPTGroup>()

    fun getGroup(name: String): Option<OpenAIGPTGroup> {
        return store[name].toOption()
    }

    fun createGroup(name: String) : Boolean {
        return store.putIfAbsent(name, OpenAIGPTGroup(60, 20)) == null
    }
}