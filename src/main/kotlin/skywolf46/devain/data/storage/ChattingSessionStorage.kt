package skywolf46.devain.data.storage

import arrow.core.Option
import arrow.core.toOption
import skywolf46.devain.data.parsed.gpt.sessions.ChattingSession
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ChattingSessionStorage {
    private val sessions = mutableMapOf<Pair<Long, Long>, ChattingSession>()
    private val lock = ReentrantLock()

    fun acquireSession(guildId: Long, userId: Long): Option<ChattingSession> {
        return sessions[guildId to userId].toOption()
    }

    fun removeSession(guildId: Long, userId: Long): Boolean {
        return lock.withLock {
            sessions.remove(guildId to userId) != null
        }
    }

    fun resetSession(guildId: Long, userId: Long, model: String) {
        lock.withLock {
            sessions.put(guildId to userId, ChattingSession(userId, model))
        }
    }
}