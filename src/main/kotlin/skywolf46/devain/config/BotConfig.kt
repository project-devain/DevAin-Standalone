package skywolf46.devain.config

import arrow.core.getOrElse
import skywolf46.devain.util.TimeUtil
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

class BotConfig {
    val botToken: String
    val openAIToken: String
    val cohereToken: String
    val openWeatherToken: String
    val googleApiToken: String
    val googleSearchEngineId: String
    val deepLToken: String
    val dreamStudioToken: String
    val maxInput: Int
    val maxDialogCache: Int
    val maxUserPresetPerServer: Int
    val maxServerPreset: Int
    val sessionTokenRestoreTimer: Long
    val sessionTokenRestoreAmount: Long
    val maxSessionToken: Long

    init {
        val file = File("settings.properties")
        val defaultProperty = Properties().apply {
            setProperty("bot-token", "your-bot-token-here")
            setProperty("cohere-token", "your-cohere-token-here")
            setProperty("openai-token", "your-openai-token-here")
            setProperty("deepl-token", "your-deepl-token-here")
            setProperty("dream-studio-token", "your-dream-studio-token-here")
            setProperty("open-weather-token", "your-open-weather-token-here")
            setProperty("google-api-token", "your-google-api-token-here")
            setProperty("google-search-engine-id", "your-google-search-engine-id-here")
            setProperty("max-input", "150")
            setProperty("max-dialog-cache", "10")
            setProperty("max-user-pattern-per-server", "10")
            setProperty("max-server-preset", "15")
            setProperty("session-token-restore-timer", "1h")
            setProperty("session-token-restore-amount", "3000")
            setProperty("max-session-token", "15000")
        }
        if (!file.exists()) {
            FileWriter(file).use {
                defaultProperty.store(it, "DevAin Config")
            }
        }
        val property = FileReader(file).use {
            Properties(defaultProperty).apply {
                load(it)
            }
        }
        botToken = property.getProperty("bot-token") ?: throw IllegalStateException("초기화 실패; 봇 토큰이 존재하지 않습니다.")
        cohereToken = property.getProperty("cohere-token")
            ?: throw IllegalStateException("초기화 실패; Cohere 토큰이 존재하지 않습니다.")
        openAIToken =
            property.getProperty("openai-token") ?: throw IllegalStateException("초기화 실패; OpenAI 토큰이 존재하지 않습니다.")
        dreamStudioToken =
            property.getProperty("dream-studio-token")
                ?: throw IllegalStateException("초기화 실패; DreamStudio 토큰이 존재하지 않습니다.")
        deepLToken =
            property.getProperty("deepl-token")
                ?: throw IllegalStateException("초기화 실패; DeepL 토큰이 존재하지 않습니다.")
        openWeatherToken =
            property.getProperty("open-weather-token")
                ?: throw IllegalStateException("초기화 실패; OpenWeather 토큰이 존재하지 않습니다.")
        googleApiToken =
            property.getProperty("google-api-token") ?: throw IllegalStateException("초기화 실패; Google API 토큰이 존재하지 않습니다.")
        googleSearchEngineId =
            property.getProperty("google-search-engine-id")
                ?: throw IllegalStateException("초기화 실패; Google Search Engine ID가 존재하지 않습니다.")
        maxInput = property.getProperty("max-input")?.toIntOrNull()
            ?: throw IllegalStateException("초기화 실패; 최대 입력 텍스트가 숫자가 아닙니다.")
        maxDialogCache = property.getProperty("max-dialog-cache")?.toIntOrNull()
            ?: throw IllegalStateException("초기화 실패; 최대 다이얼로그 캐싱 개수가 숫자가 아닙니다.")
        maxUserPresetPerServer = property.getProperty("max-user-pattern-per-server")?.toIntOrNull()
            ?: throw IllegalStateException("초기화 실패; 유저당 최대 패턴 개수가 숫자가 아닙니다.")
        maxServerPreset = property.getProperty("max-server-preset")?.toIntOrNull()
            ?: throw IllegalStateException("초기화 실패; 서버당 최대 프리셋 개수가 숫자가 아닙니다.")
        sessionTokenRestoreTimer =
            TimeUtil.parseToLong(property.getProperty("session-token-restore-timer") ?: "1h").getOrElse {
                throw IllegalStateException("초기화 실패; 세션 토큰 재생 주기가 시간 포맷이 아닙니다.")
            }
        sessionTokenRestoreAmount = property.getProperty("session-token-restore-amount")?.toLongOrNull()
            ?: throw IllegalStateException("초기화 실패; 재생 주기당 토큰 재생 개수가 숫자가 아닙니다.")
        maxSessionToken = property.getProperty("max-session-token")?.toLongOrNull()
            ?: throw IllegalStateException("초기화 실패; 최대 세션 토큰 개수가 숫자가 아닙니다.")
        println("..설정 불러오기 완료 : ")
        println("..최대 입력 허용: ${maxInput}자")
        println("..최대 다이얼로그 캐싱: ${maxDialogCache}개")
    }

}