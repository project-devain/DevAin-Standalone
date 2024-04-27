package skywolf46.devain.util

fun String.replaceAllArgument(args: Map<String, String>): String {
    return (listOf(this to "") + args.toList()).reduce { acc, pair ->
        acc.first.replace("{${pair.first}}", pair.second) to ""
    }.first
}