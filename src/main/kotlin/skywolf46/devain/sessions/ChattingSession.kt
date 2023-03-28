package skywolf46.devain.sessions

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import org.json.simple.JSONArray
import org.json.simple.JSONObject

// 각 세션은 2시간 뒤에 만료됩니다.
const val SESSION_EXPIRE_AFTER = 1000 * 60 * 60 * 2

class ChattingSession(val userId: Long) {
    private var channel: TextChannel? = null
    private var sessionExpire = System.currentTimeMillis() + SESSION_EXPIRE_AFTER
        private set
    private val dialogs = mutableListOf<Pair<DialogType, String>>()

    fun updateLastMessageChannel(channel: TextChannel) {
        this.channel = channel
    }

    fun addDialog(type: DialogType, dialog: String) : ChattingSession{
        dialogs.add(type to dialog)
        sessionExpire = System.currentTimeMillis() + SESSION_EXPIRE_AFTER
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