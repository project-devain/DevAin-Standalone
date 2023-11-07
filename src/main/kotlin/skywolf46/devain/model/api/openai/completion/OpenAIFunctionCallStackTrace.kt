package skywolf46.devain.model.api.openai.completion

import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import skywolf46.devain.util.TimeUtil
import java.text.SimpleDateFormat

class OpenAIFunctionCallStackTrace {
    companion object {
        private val timeFormat = SimpleDateFormat("mm:ss")
    }

    private val usedFunctions = mutableListOf<FunctionTrace>()

    private val traceStart = System.currentTimeMillis()

    fun addFunction(
        type: TraceType,
        function: OpenAIFunctionKey,
        time: Long = System.currentTimeMillis(),
        additionalInfo: Option<String> = None
    ): OpenAIFunctionCallStackTrace {
        usedFunctions.add(FunctionTrace(type, function, time, additionalInfo))
        return this
    }

    fun buildStackTrace(): String {
        if (usedFunctions.isEmpty())
            return "```펑션 호출 스택 없음```"
        val builder = StringBuilder("```ansi\n")
        for (trace in usedFunctions) {
            builder.appendLine(trace.toString(traceStart))
        }
        builder.append("```")
        return builder.toString()
    }

    fun size() = usedFunctions.size


    enum class TraceType(val prefix: String) {
        NORMAL(color(false, 32) + "────>"),
        FATAL(color(false, 31) + "─${color(false, 30)}//${color(false, 31)}─>"),
        ERROR(color(false, 32) + "──${color(false, 31)}──>${color(false, 33)}"),
    }


    data class FunctionTrace(
        val type: TraceType,
        val function: OpenAIFunctionKey,
        val time: Long,
        val additionalInfo: Option<String> = None
    ) {
        fun toString(traceStart: Long): String {
            return "${type.prefix} $function (${TimeUtil.toTimeString(time - traceStart)})" + additionalInfo.map {
                "\n └─── ${
                    color(
                        false,
                        37
                    )
                }$it"
            }
                .getOrElse { "" }
        }
    }
}

private fun color(bold: Boolean, colorId: Int): String {
    return "\u001B[${if (bold) 1 else 0};${colorId}m"
}

private fun reset(): String {
    return "\u001B[0m"
}