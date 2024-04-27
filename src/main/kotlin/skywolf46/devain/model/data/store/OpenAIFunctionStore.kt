package skywolf46.devain.model.data.store

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.toOption
import skywolf46.devain.model.api.openai.completion.OpenAIFunctionDeclaration
import skywolf46.devain.model.api.openai.completion.OpenAIFunctionKey

class OpenAIFunctionStore {

    private val functions = mutableMapOf<OpenAIFunctionKey, OpenAIFunctionDeclaration>()
    fun registerFunction(function: OpenAIFunctionDeclaration): OpenAIFunctionStore {
        functions[function.key] = function
        return this
    }

    fun getFunction(key: OpenAIFunctionKey): Option<OpenAIFunctionDeclaration> {
        return functions[key].toOption()
    }

    fun getFunctionOrEmpty(key: OpenAIFunctionKey): OpenAIFunctionDeclaration {
        return getFunction(key).getOrElse {
            OpenAIFunctionDeclaration(
                OpenAIFunctionKey(key.functionName, OpenAIFunctionKey.FunctionFlag.INVALID),
                "Invalid function",
                emptyList()
            )
        }
    }
}