package skywolf46.devain.util

import arrow.core.*

object TimeUtil {
    private val timeDelimiters = mutableMapOf(
        "d" to 24 * 60 * 60 * 1000L,
        "h" to 60 * 60 * 1000L,
        "m" to 60 * 1000L,
        "s" to 1000L,
    )

    private val timeDelimitersKorean = mutableMapOf(
        "일" to 24 * 60 * 60 * 1000L,
        "시간" to 60 * 60 * 1000L,
        "분" to 60 * 1000L,
        "초" to 1000L,
    )

    private val timeStringDelimiters = timeDelimitersKorean.toList()

    fun toTimeString(time: Long): String {
        var totalTime = time
        return StringBuilder().apply {
            for ((unit, amount) in timeStringDelimiters) {
                val target = totalTime / amount
                if (target > 0) {
                    totalTime -= target * amount
                    append(target).append(unit).append(" ")
                }
            }
            if (isEmpty()) {
                return "0${timeStringDelimiters[timeStringDelimiters.size - 1].first}"
            }
        }.trimEnd().toString()
    }

    fun parseToLong(time: String): Either<String, Long> {
        "".toOption().and("".toOption())
        return parseToLong(time, timeDelimiters)
    }

    fun parseToLong(time: String, delimiters: Map<String, Long>): Either<String, Long> {
        val target = time.replace(" ", "")
        if (target.replace(Regex("[0-9dhms]"), "").isNotEmpty()) {
            return "Time string contains illegal characters (${
                target.replace(Regex("[0-9dhms]"), "").toList().distinct().joinToString(", ")
            })".left()
        }
        return parseTimeString(target, delimiters)
    }

    private fun parseTimeString(time: String, delimiters: Map<String, Long>): Either<String, Long> {
        return mutableListOf(time.findDelimiter(0, delimiters).getOrElse { return it.left() }).apply {
            while (get(size - 1).first != -1) {
                add(time.findDelimiter(get(size - 1).first, delimiters).getOrElse { return it.left() })
            }
            removeAt(size - 1)
        }.sumOf { it.second }.right()
    }

    private fun String.findDelimiter(index: Int, delimiters: Map<String, Long>): Either<String, Pair<Int, Long>> {
        for (x in index until length) {
            if (get(x) in '0'..'9') {
                continue
            }
            val delimiterIndex = acquireDelimiterEndIndexFrom(x, delimiters).getOrElse {
                return it.left()
            }
            return (delimiterIndex to (substring(index, x).toLong() * delimiters[substring(
                x,
                delimiterIndex
            )]!!)).right()
        }
        return (-1 to -1L).right()
    }

    private fun String.acquireDelimiterEndIndexFrom(index: Int, delimiters: Map<String, Long>): Either<String, Int> {
        for (x in index until length) {
            val string = substring(index, x + 1)
            if (delimiters.containsKey(string))
                return (x + 1).right()
        }
        return "No valid delimiters found from string (Index ${index}, remaining string ${substring(index)})".left()
    }
}