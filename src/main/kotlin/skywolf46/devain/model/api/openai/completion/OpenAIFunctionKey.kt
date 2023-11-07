package skywolf46.devain.model.api.openai.completion

data class OpenAIFunctionKey(
    /**
     * Function name.
     */
    val functionName: String,
    /**
     * Function source.
     */
    val source: FunctionFlag = FunctionFlag.BUILT_IN
) {
    enum class FunctionFlag {
        BUILT_IN,
        USER_SPECIFIED,
        ROOM_SPECIFIED,
        INVALID
    }

    override fun toString(): String {
        return "${functionName}@${source.name}"
    }
}