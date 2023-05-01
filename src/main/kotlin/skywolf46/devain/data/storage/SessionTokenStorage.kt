package skywolf46.devain.data.storage

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import skywolf46.devain.config.BotConfig
import skywolf46.devain.data.parsed.gpt.sessions.SessionTokenData
import java.sql.Connection
import java.sql.DriverManager

class SessionTokenStorage : KoinComponent {
    private val connection: Connection
    private val botConfig by inject<BotConfig>()

    init {
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:sessions.db")
        kotlin.runCatching {
            connection.prepareStatement("create table if not exists session_token(userId BIGINT primary key, sessionToken BIGINT, regenerateStart BIGINT default 0)")
                .use {
                    it.execute()
                }
        }.onFailure {
            it.printStackTrace()
        }
    }


    fun estimateToken(userId: Long): SessionTokenData {
        connection.prepareStatement("select * from session_token where userId = ?").use {
            it.setLong(1, userId)
            it.executeQuery().use { set ->
                if (!set.next())
                    return SessionTokenData(userId, botConfig.maxSessionToken, true, System.currentTimeMillis())
                return SessionTokenData.of(set)
            }
        }
    }

    fun withdrawIfEnough(userId: Long, amount: Int): Boolean {
        val tokenData = estimateToken(userId)
        if (tokenData.tokenAmount >= amount) {
            update(tokenData.updateTokenAmount(botConfig, tokenData.tokenAmount - amount))
            return true
        }
        return false
    }

    fun withdraw(userId: Long, amount: Int) {
        val tokenData = estimateToken(userId)
        update(tokenData.updateTokenAmount(botConfig, (tokenData.tokenAmount - amount).coerceAtLeast(0)))

    }

    fun update(data: SessionTokenData) {
        connection.prepareStatement("replace into session_token values (?, ?, ?)").use {
            it.setLong(1, data.userId)
            it.setLong(2, data.tokenAmount)
            it.setLong(3, data.regenerateStart)
            it.executeUpdate()
        }
    }

}