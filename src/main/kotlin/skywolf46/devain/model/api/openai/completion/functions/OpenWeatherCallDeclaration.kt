package skywolf46.devain.model.api.openai.completion.functions

import arrow.core.getOrElse
import org.json.simple.JSONObject
import org.koin.core.component.inject
import skywolf46.devain.apicall.networking.GetRequest
import skywolf46.devain.controller.api.requests.openweather.OpenWeatherAPICall
import skywolf46.devain.model.api.openai.OpenAIParameterSchema
import skywolf46.devain.model.api.openai.completion.OpenAIFunctionDeclaration
import skywolf46.devain.model.api.openai.completion.OpenAIFunctionKey
import skywolf46.devain.util.getMap

class OpenWeatherCallDeclaration : OpenAIFunctionDeclaration(
    OpenAIFunctionKey("weather", OpenAIFunctionKey.FunctionFlag.BUILT_IN),
    "OpenWeather API를 사용하여 대상 지역의 현재 날씨를 불러옵니다. 해당 펑션을 사용하였다면, 정보의 출처를 출력하십시오. 반환되는 온도는 섭씨 기준입니다. ",
    listOf(
        OpenAIParameterSchema(
            "location",
            "날씨를 불러올 지역입니다. 이 파라미터는 반드시 영어여야 합니다. ex) Seoul",
            "string",
            "true"
        )
    )
) {

    private val apiCall by inject<OpenWeatherAPICall>()
    override suspend fun call(param: JSONObject): JSONObject {
        return apiCall.call(GetRequest(param["location"].toString())).getOrElse {
            return JSONObject().apply {
                put("error", it.getErrorMessage())
            }
        }.json.apply {
            val mainWeather = getMap("main")
            if ("temp" in mainWeather) {
                mainWeather["temp"] = (mainWeather["temp"].toString().toDouble()) - 273.15
            }
            if ("max_temp" in mainWeather) {
                mainWeather["max_temp"] = (mainWeather["max_temp"].toString().toDouble()) - 273.15
            }
            if ("min_temp" in mainWeather) {
                mainWeather["min_temp"] = (mainWeather["min_temp"].toString().toDouble()) - 273.15
            }
            if ("feels_like" in mainWeather) {
                mainWeather["feels_like"] =
                    (mainWeather["feels_like"].toString().toDouble()) - 273.15
            }
        }
    }
}