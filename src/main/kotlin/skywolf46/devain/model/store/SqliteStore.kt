package skywolf46.devain.model.store

import java.sql.Connection
import java.sql.DriverManager
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class SqliteStore {
    init {
        Class.forName("org.sqlite.JDBC")
    }

    private val store = mutableMapOf<String, Connection>()
    private val lock = ReentrantReadWriteLock()

    fun getConnection(key: String): Connection {
        lock.read {
            if (key in store)
                return store[key]!!
        }
        lock.write {
            if (key in store)
                return store[key]!!
            store[key] = DriverManager.getConnection("jdbc:sqlite:$key.db")
            return store[key]!!
        }
    }
}