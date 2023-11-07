package skywolf46.devain.model.api.openai.completion.functions

import arrow.core.getOrElse
import org.json.simple.JSONObject
import org.koin.core.component.inject
import skywolf46.devain.controller.api.requests.openweather.OpenWeatherForecastAPICall
import skywolf46.devain.model.api.openai.GetRequest
import skywolf46.devain.model.api.openai.OpenAIParameterSchema
import skywolf46.devain.model.api.openai.completion.OpenAIFunctionDeclaration
import skywolf46.devain.model.api.openai.completion.OpenAIFunctionKey
import skywolf46.devain.util.getList
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OpenWeatherForecastDeclaration : OpenAIFunctionDeclaration(
    OpenAIFunctionKey("weather_forecast", OpenAIFunctionKey.FunctionFlag.BUILT_IN),
    "OpenWeather API를 사용하여 대상 지역의 24시간 내의 날씨 예보를 불러옵니다. 현재 시간이 아닌 특정 시간(아침, 점심, 저녁..)이 지정되었을 경우, 해당 펑션을 사용하십시오. 해당 펑션을 사용하였다면, 정보의 출처를 출력하십시오. 반환되는 온도는 섭씨 기준입니다. ",
    listOf(
        OpenAIParameterSchema(
            "location",
            "날씨를 불러올 지역입니다. 이 파라미터는 반드시 영어여야 합니다. ex) Seoul",
            "string",
            "true"
        )
    )
) {
    private val apiCall by inject<OpenWeatherForecastAPICall>()

    private val dateTimeFormatFrom = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val dateTimeFormatTo = DateTimeFormatter.ofPattern("hh:mm a")

    override suspend fun call(param: JSONObject): JSONObject {
        val apiCallResult = apiCall.call(GetRequest(param["location"].toString())).getOrElse {
            return JSONObject().apply {
                put("error", it.getErrorMessage())
            }
        }.json
        val result = JSONObject()
        result["weather"] = apiCallResult.getList("list").subList(0, 8).onEach { outer ->
            outer as JSONObject
            val mainWeather = outer["main"] as JSONObject

            if ("temp" in mainWeather) {
                mainWeather["temp"] = (mainWeather["temp"].toString().toDouble()) - 273.15
            }
            if ("max_temp" in mainWeather) {
                mainWeather["max_temp"] =
                    (mainWeather["max_temp"].toString().toDouble()) - 273.15
            }
            if ("min_temp" in mainWeather) {
                mainWeather["min_temp"] =
                    (mainWeather["min_temp"].toString().toDouble()) - 273.15
            }

            if ("feels_like" in mainWeather) {
                mainWeather["feels_like"] =
                    (mainWeather["feels_like"].toString().toDouble()) - 273.15
            }
            val currentTime =
                LocalDateTime.parse(outer["dt_txt"].toString(), dateTimeFormatFrom)
            outer["time"] = dateTimeFormatTo.format(currentTime.plusHours(9))
            outer.remove("dt")
            outer.remove("dt_txt")
        }
        return result
    }
}