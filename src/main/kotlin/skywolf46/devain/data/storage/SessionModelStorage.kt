package skywolf46.devain.data.storage

import arrow.core.Option
import arrow.core.toOption
import skywolf46.devain.data.parsed.gpt.sessions.SessionModelData

class SessionModelStorage {
    private val sessionModelData = mutableMapOf<String, SessionModelData>()

    fun registerSessionModel(data: SessionModelData) {
        sessionModelData[data.model] = data
    }

    fun getModelData(modelName: String): Option<SessionModelData> {
        return sessionModelData[modelName].toOption()
    }

    init {
        registerSessionModel(SessionModelData("gpt-3.5-turbo", 150.0, 1.0, true))
        registerSessionModel(SessionModelData("gpt-4", 750.0, 3.75, true))
    }
}