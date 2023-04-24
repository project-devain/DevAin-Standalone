package skywolf46.devain.data

import java.sql.ResultSet

data class PresetData(val name: String, val contents: String, val isShared: Boolean) {
    constructor(set: ResultSet) : this(set.getString(1), set.getString(2), set.getBoolean(3) )
}