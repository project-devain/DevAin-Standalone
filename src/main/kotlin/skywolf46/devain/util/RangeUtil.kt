package skywolf46.devain.util

inline fun Int.checkRangeAndFatal(range: IntRange, fatal: (IntRange) -> Unit) : Int{
    if (this !in range)
        fatal(range)
    return this
}


inline fun Double.checkRangeAndFatal(range: ClosedRange<Double>, fatal: (ClosedRange<Double>) -> Unit) : Double {
    if (this !in range)
        fatal(range)
    return this
}