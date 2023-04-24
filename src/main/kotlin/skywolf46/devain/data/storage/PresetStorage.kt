package skywolf46.devain.data.storage

import arrow.core.Option
import arrow.core.orElse
import arrow.core.toOption
import kotlinx.coroutines.runBlocking
import skywolf46.devain.data.PresetData
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class PresetStorage {
    private val connection: Connection

    init {
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:presets.db");
        kotlin.runCatching {
            connection.prepareStatement("create table if not exists user_preset(serverId BIGINT, userId BIGINT, presetId VARCHAR(12), preset TEXT, isShared INT, primary key (serverId, userId, presetId))")
                .use {
                    it.execute()
                }
            connection.prepareStatement("create table if not exists server_preset(serverId BIGINT, presetId VARCHAR(12), preset TEXT,  primary key (serverId, presetId))")
                .use {
                    it.execute()
                }
        }.onFailure {
            it.printStackTrace()
        }
    }

    fun getPresets(serverId: Long, userId: Long): Map<String, PresetData> {
        return runBlocking {
            kotlin.runCatching {
                return@runBlocking mutableMapOf<String, PresetData>().apply {
                    loadPresets(serverId, userId) {
                        while (it.next()) {
                            put(it.getString(1), PresetData(it))
                        }
                    }
                }
            }.onFailure {
                it.printStackTrace()
            }.getOrElse {
                mutableMapOf()
            }
        }
    }

    fun getPresetList(serverId: Long, userId: Long, includeServer: Boolean) : List<String>{
        return if (includeServer) {
            getUserPresetList(serverId, userId) + getServerPresetList(serverId)
        } else {
            getUserPresetList(serverId, userId)
        }
    }

    private fun getUserPresetList(serverId: Long, userId: Long): List<String> {
        return mutableListOf<String>().apply {
            connection.prepareStatement("select presetId from user_preset where serverId = ? and userId = ?")
                .use {
                    it.setLong(1, serverId)
                    it.setLong(2, userId)
                    it.executeQuery().use { set ->
                        while (set.next()) {
                            add(set.getString(1))
                        }
                    }
                }
        }
    }

    fun getServerPresetList(serverId: Long): List<String> {
        return mutableListOf<String>().apply {
            connection.prepareStatement("select presetId from server_preset where serverId = ?")
                .use {
                    it.setLong(1, serverId)
                    it.executeQuery().use { set ->
                        while (set.next()) {
                            add(set.getString(1))
                        }
                    }
                }
        }
    }

    private fun loadPresets(serverId: Long, userId: Long, unit: (ResultSet) -> Unit) {
        return connection.prepareStatement("select presetId, preset, isShared from user_preset where serverId = ? and userId = ?")
            .use {
                it.setLong(1, serverId)
                it.setLong(2, userId)
                unit(it.executeQuery())
            }
    }

    fun getPresetById(serverId: Long, userId: Long, presetName: String, includeServer: Boolean): Option<PresetData> {
        return if (includeServer) {
            findUserPreset(serverId, userId, presetName).orElse { getServerPresetById(serverId, presetName) }
        } else {
            findUserPreset(serverId, userId, presetName)
        }
    }

    fun getServerPresetById(serverId: Long, presetId: String): Option<PresetData> {
        connection.prepareStatement("select presetId, preset, isShared from user_preset where serverId = ? and presetId = ?")
            .use { statement ->
                statement.setLong(1, serverId)
                statement.setString(2, presetId)
                statement.executeQuery().use {
                    if (it.next()) {
                        return PresetData(it).toOption()
                    }
                }
            }
        return Option.fromNullable(null)
    }

    private fun findUserPreset(serverId: Long, userId: Long, presetId: String): Option<PresetData> {
        connection.prepareStatement("select presetId, preset, isShared from user_preset where serverId = ? and userId = ? and presetId = ?")
            .use { statement ->
                statement.setLong(1, serverId)
                statement.setLong(2, userId)
                statement.setString(3, presetId)
                statement.executeQuery().use {
                    if (it.next()) {
                        return PresetData(it).toOption()
                    }
                }
            }
        return Option.fromNullable(null)
    }


    fun updatePreset(serverId: Long, userId: Long, name: String, preset: String, isShared: Boolean) {
        connection.prepareStatement("replace into user_preset values(?, ?, ?, ?, ?)").use {
            it.setLong(1, serverId)
            it.setLong(2, userId)
            it.setString(3, name)
            it.setString(4, preset)
            it.setBoolean(5, isShared)
            it.executeUpdate()
        }
    }


}