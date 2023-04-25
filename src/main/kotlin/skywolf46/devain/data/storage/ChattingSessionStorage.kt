package skywolf46.devain.data.storage

import skywolf46.devain.data.sessions.ChattingSession
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ChattingSessionStorage {
    private val sessions = mutableMapOf<Pair<Long, Long>, ChattingSession>()
    private val lock = ReentrantLock()

    fun removeSession(guildId: Long, userId: Long): Boolean {
        return lock.withLock {
            sessions.remove(guildId to userId) != null
        }
    }

    fun resetSession(guildId: Long, userId: Long) {
        lock.withLock {
            sessions.put(guildId to userId, ChattingSession(userId))
        }
    }
}