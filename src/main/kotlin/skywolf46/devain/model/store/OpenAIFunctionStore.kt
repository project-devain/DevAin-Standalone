package skywolf46.devain.model.store

import org.json.simple.JSONObject
import skywolf46.devain.model.rest.openai.completion.request.OpenAIFunctionDeclaration

class OpenAIFunctionStore {
    private val functions = mutableMapOf<OpenAIFunctionDeclaration, (JSONObject) -> String>()

    fun registerFunction(declaration: OpenAIFunctionDeclaration, function: (JSONObject) -> String) {
        functions[declaration] = function
    }


}