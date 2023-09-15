package skywolf46.devain.util

inline fun Int.checkRangeAndFatal(range: IntRange, fatal: (IntRange) -> Unit) {
    if (this !in range)
        fatal(range)
}


inline fun Double.checkRangeAndFatal(range: ClosedRange<Double>, fatal: (ClosedRange<Double>) -> Unit) {
    if (this !in range)
        fatal(range)
}