package skywolf46.devain.controller.api.requests.openai

import arrow.core.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.koin.core.component.get
import org.koin.core.component.inject
import skywolf46.devain.*
import skywolf46.devain.apicall.APICall
import skywolf46.devain.apicall.APIError
import skywolf46.devain.apicall.errors.PreconditionError
import skywolf46.devain.apicall.errors.StandardRestAPIError
import skywolf46.devain.apicall.errors.UnexpectedError
import skywolf46.devain.controller.api.requests.devain.DevAinUpdatePersistenceCountAPICall
import skywolf46.devain.model.api.openai.UpdateRequest
import skywolf46.devain.model.api.openai.completion.*
import skywolf46.devain.model.data.store.OpenAIFunctionStore
import skywolf46.devain.util.parseMap

private const val OPENAI_GPT_COMPLETION_ENDPOINT = "https://api.openai.com/v1/chat/completions"
private const val FUNCTION_CALL_DEPTH_LIMIT = 6

class GPTCompletionAPICall(private val apiKey: String, client: Option<HttpClient> = None) :
    APICall<OpenAIGPTRequest, OpenAIGPTResponse> {
    private val client = client.getOrElse { get() }
    private val parser = get<JSONParser>()
    private val updateCall = get<DevAinUpdatePersistenceCountAPICall>()
    private val functionStore by inject<OpenAIFunctionStore>()

    override suspend fun call(request: OpenAIGPTRequest): Either<APIError, OpenAIGPTResponse> {
        return call(request, OpenAIFunctionCallStackTrace())
    }

    private suspend fun call(
        request: OpenAIGPTRequest,
        stackTrace: OpenAIFunctionCallStackTrace
    ): Either<APIError, OpenAIGPTResponse> {
        return runCatching {
            val prebuiltRequest = request.serialize().getOrElse { return PreconditionError(it).left() }
            val result = client.post(OPENAI_GPT_COMPLETION_ENDPOINT) {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $apiKey")
                }
                setBody(prebuiltRequest.toJSONString())
                println(prebuiltRequest.toJSONString())
            }
            if (result.status.value != 200) {
                return StandardRestAPIError(result.status.value, result.bodyAsText()).left()
            }
            println("GPT -> ${parser.parseMap(result.bodyAsText()).toJSONString()}")
            val parsedResult = OpenAIGPTResponse.fromJson(parser.parseMap(result.bodyAsText()), stackTrace)
            updateCall.call(UpdateRequest(KEY_GPT_PROCEED_COUNT, 1L))
            updateCall.call(UpdateRequest(KEY_GPT_PROCEED_TOKEN, parsedResult.usage.completionToken.toLong()))
            updateCall.call(UpdateRequest(KEY_GPT_PROCEED_TIME, (System.currentTimeMillis() - request.createdOn)))
            if (parsedResult.answers[0].reason == OpenAIGPTAnswer.FinishReason.FUNCTION_CALL) {
                return callFunction(request, parsedResult, stackTrace)
            }
            parsedResult.right()
        }.getOrElse {
            it.printStackTrace()
            UnexpectedError(it).left()
        }
    }

    private suspend fun callFunction(
        request: OpenAIGPTRequest,
        response: OpenAIGPTResponse,
        stackTrace: OpenAIFunctionCallStackTrace
    ): Either<APIError, OpenAIGPTResponse> {
        val functionRequest = response.answers[0].message.functionCall.getOrNull()!!
        val function = functionStore.getFunctionOrEmpty(
            OpenAIFunctionKey(
                functionRequest["name"]!!.toString(),
                OpenAIFunctionKey.FunctionFlag.BUILT_IN
            )
        )
        if (stackTrace.size() >= FUNCTION_CALL_DEPTH_LIMIT) {
            stackTrace.addFunction(OpenAIFunctionCallStackTrace.TraceType.FATAL, function.key)
            return UnexpectedError(
                StackOverflowError(
                    "GPT 모델이 펑션 호출 깊이 제한(${FUNCTION_CALL_DEPTH_LIMIT})을 벗어났습니다. \n스택 트레이스:\n ${
                        stackTrace.buildStackTrace()
                    }"
                )
            ).left()
        }
        val start = System.currentTimeMillis()
        println("GPT requested function ${functionRequest["name"]} with arguments ${functionRequest["arguments"]}")
        request.messages.add(response.answers[0].message)
        request.messages.add(
            parseResponse(
                functionRequest,
                function,
                stackTrace
            )
        )
        updateCall.call(UpdateRequest(KEY_GPT_FUNCTION_PROCEED_COUNT, 1L))
        updateCall.call(UpdateRequest(KEY_GPT_FUNCTION_PROCEED_TIME, (System.currentTimeMillis() - start)))
        return call(request, stackTrace)
    }

    private suspend fun parseResponse(
        request: JSONObject,
        function: OpenAIFunctionDeclaration,
        trace: OpenAIFunctionCallStackTrace
    ): OpenAIGPTMessage {
        return if (function.key.source == OpenAIFunctionKey.FunctionFlag.INVALID) {
            trace.addFunction(OpenAIFunctionCallStackTrace.TraceType.ERROR, function.key)
            OpenAIGPTMessage(
                OpenAIGPTMessage.Role.FUNCTION,
                content = "No function named ${function.key.functionName}".toOption(),
                functionName = function.key.functionName.toOption()
            )
        } else {
            val param = parser.parseMap(request["arguments"].toString())
            trace.addFunction(
                OpenAIFunctionCallStackTrace.TraceType.NORMAL, function.key,
                additionalInfo = if (param.isEmpty()) {
                    None
                } else {
                    param.map { it.key to it.value }.joinToString(", ") {
                        "${it.first}: ${it.second}"
                    }.toOption()
                }
            )
            OpenAIGPTMessage(
                OpenAIGPTMessage.Role.FUNCTION,
                content = function.call(param)
                    .toJSONString().toOption(),
                functionName = function.key.functionName.toOption()
            )
        }
    }


}