package skywolf46.devain.data.parsed.gpt.sessions

import arrow.core.getOrElse
import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.EncodingType
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import skywolf46.devain.data.storage.SessionModelStorage
import java.util.concurrent.atomic.AtomicBoolean

// 각 세션은 2시간 뒤에 만료됩니다.
const val SESSION_EXPIRE_AFTER = 1000 * 60 * 60 * 2

class ChattingSession(val userId: Long, val model: String) : KoinComponent {
    private val modelDataStorage by inject<SessionModelStorage>()


    private var sessionExpire = System.currentTimeMillis() + SESSION_EXPIRE_AFTER

    private val dialogs = mutableListOf<Pair<DialogType, String>>()

    private val isSessionOnUse = AtomicBoolean(false)

    var sessionUsage = 0
        private set

    val modelData by lazy {
        modelDataStorage.getModelData(model).getOrElse { throw IllegalStateException() }
    }

    fun acquireSession(): Boolean {
        if (isSessionOnUse.get())
            return false
        isSessionOnUse.set(true)
        return true
    }

    fun releaseSession() {
        isSessionOnUse.set(false)
    }

    fun getUsageWith(dialog: String): Int {
        return sessionUsage + Encodings.newDefaultEncodingRegistry().getEncoding(EncodingType.CL100K_BASE)
            .encode(dialog).size
    }


    fun addDialog(type: DialogType, dialog: String): ChattingSession {
        dialogs.add(type to dialog)
        sessionExpire = System.currentTimeMillis() + SESSION_EXPIRE_AFTER
        sessionUsage += Encodings.newDefaultEncodingRegistry().getEncoding(EncodingType.CL100K_BASE).encode(dialog).size
        return this
    }

    fun addDialog(type: DialogType, dialog: String, token: Int): ChattingSession {
        dialogs.add(type to dialog)
        sessionExpire = System.currentTimeMillis() + SESSION_EXPIRE_AFTER
        sessionUsage += token
        return this
    }

    fun applyDialogLimit(size: Int) {
        while ((dialogs.size / 2) >= size) {
            dialogs.removeAt(0)
            dialogs.removeAt(0)
        }
    }

    fun toJson(): JSONArray {
        val array = JSONArray()
        for (x in dialogs) {
            array.add(JSONObject().apply {
                put("role", x.first.name.lowercase())
                put("content", x.second.replace("<", "\\<").replace(">", "\\>"))
            })
        }
        return array
    }


    enum class DialogType {
        USER, ASSISTANT
    }
}