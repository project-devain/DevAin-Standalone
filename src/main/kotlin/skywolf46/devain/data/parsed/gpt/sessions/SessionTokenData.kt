package skywolf46.devain.data.parsed.gpt.sessions

import org.koin.java.KoinJavaComponent
import skywolf46.devain.config.BotConfig
import java.sql.ResultSet
import kotlin.math.roundToInt

data class SessionTokenData(
    val userId: Long,
    val tokenAmount: Long,
    val isFullToken: Boolean,
    val regenerateStart: Long
) {

    fun updateTokenAmount(config: BotConfig, amount: Long): SessionTokenData {
        if (amount < config.maxSessionToken)
            return if (isFullToken)
                copy(tokenAmount = amount, isFullToken = false, regenerateStart = System.currentTimeMillis())
            else
                copy(tokenAmount = amount, isFullToken = false)
        return copy(tokenAmount = amount, isFullToken = true)
    }

    fun estimateLeftRegenerateTime(config: BotConfig): Long {
        val leftAmount = (config.maxSessionToken - tokenAmount)
        val time = (leftAmount.toDouble() / config.sessionTokenRestoreAmount).roundToInt()
        val currentLeftTime =
            config.sessionTokenRestoreTimer - (System.currentTimeMillis() - regenerateStart) % config.sessionTokenRestoreTimer
        return (time - 1).coerceAtLeast(0) * config.sessionTokenRestoreTimer + currentLeftTime
    }


    fun estimateRegenerateEndTime(config: BotConfig): Long {
        return System.currentTimeMillis() + estimateLeftRegenerateTime(config)
    }

    companion object {
        fun of(set: ResultSet): SessionTokenData {
            val config = KoinJavaComponent.get<BotConfig>(BotConfig::class.java)
            val currentToken = set.getInt(2)
            return if (currentToken < config.maxSessionToken) {
                val regenerateCount = (System.currentTimeMillis() - set.getLong(3)) / config.sessionTokenRestoreTimer
                val nextTimer = set.getLong(3) + (regenerateCount * config.sessionTokenRestoreTimer)
                val estimatedToken =
                    config.maxSessionToken.coerceAtMost(currentToken + (regenerateCount * config.sessionTokenRestoreAmount))
                val isFullToken = estimatedToken >= config.maxSessionToken
                SessionTokenData(
                    set.getLong(1),
                    estimatedToken,
                    isFullToken,
                    if (isFullToken) System.currentTimeMillis() else nextTimer
                )
            } else {
                SessionTokenData(set.getLong(1), currentToken.toLong(), true, System.currentTimeMillis())
            }
        }
    }
}