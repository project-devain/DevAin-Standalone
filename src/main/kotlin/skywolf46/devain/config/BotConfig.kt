package skywolf46.devain.config

import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

class BotConfig(file: File) {
    val botToken: String
    val openAIToken: String
    val dreamStudioToken: String
    val maxInput: Int
    val maxDialogCache: Int

    init {
        val defaultProperty = Properties().apply {
            setProperty("bot-token", "your-bot-token-here")
            setProperty("openai-token", "your-openai-token-here")
            setProperty("dream-studio-token", "your-dream-studio-token-here")
            setProperty("max-input", "150")
            setProperty("max-dialog-cache", "10")
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
        openAIToken =
            property.getProperty("openai-token") ?: throw IllegalStateException("초기화 실패; OpenAI 토큰이 존재하지 않습니다.")
        dreamStudioToken =
            property.getProperty("dream-studio-token") ?: throw IllegalStateException("초기화 실패; DreamStudio 토큰이 존재하지 않습니다.")
        maxInput = property.getProperty("max-input")?.toIntOrNull()
            ?: throw IllegalStateException("초기화 실패; 최대 입력 텍스트가 숫자가 아닙니다.")
        maxDialogCache = property.getProperty("max-dialog-cache")?.toIntOrNull()
            ?: throw IllegalStateException("초기화 실패; 최대 다이얼로그 캐싱 개수가 숫자가 아닙니다.")
        println("..설정 불러오기 완료 : ")
        println("..최대 입력 허용: ${maxInput}자")
        println("..최대 다이얼로그 캐싱: ${maxDialogCache}개")
    }

}