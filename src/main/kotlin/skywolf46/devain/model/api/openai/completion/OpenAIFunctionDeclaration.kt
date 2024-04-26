package skywolf46.devain.model.api.openai.completion

import arrow.core.Either
import arrow.core.right
import org.json.simple.JSONObject
import skywolf46.devain.apicall.networking.Request
import skywolf46.devain.model.api.openai.OpenAIParameterSchema
import skywolf46.devain.util.putArray
import skywolf46.devain.util.putMap

open class OpenAIFunctionDeclaration(
    /**
     * Function key for call.
     */
    val key: OpenAIFunctionKey,
    /**
     * Function description.
     * GPT will use this description for function call.
     */
    val description: String,
    /**
     * Function parameter schema.
     * GPT will use this schema for function call.
     */
    val parameterSchema: List<OpenAIParameterSchema>
) : Request<JSONObject> {

    open suspend fun call(param: JSONObject): JSONObject {
        return JSONObject()
    }

    override fun serialize(): Either<Throwable, JSONObject> {
        return JSONObject().apply {
            put("name", key.functionName)
            put("description", description)
            put("parameters", buildParameterJson())
        }.right()
    }

    private fun buildParameterJson(): JSONObject {
        return JSONObject().apply {
            put("type", "object")
            putMap(
                "properties",
                *parameterSchema.map {
                    it.parameterName to it.serialize().getOrNull()!!
                }.toTypedArray()
            )
            putArray(
                "required",
                *parameterSchema.filter { it.required == "true" }.map { it.parameterName }.toTypedArray()
            )
        }
    }

}