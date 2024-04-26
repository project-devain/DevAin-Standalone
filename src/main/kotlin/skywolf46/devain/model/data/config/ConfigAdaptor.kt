package skywolf46.devain.model.data.config

import java.io.InputStream
import java.io.OutputStream

interface ConfigAdaptor {
    fun serializeTo(stream: OutputStream, data: Map<String, Any>)

    fun serializeFrom(stream: InputStream) : Map<String, Any>
}