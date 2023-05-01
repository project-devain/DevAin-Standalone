package skywolf46.devain.data.parsed.gpt.sessions

data class SessionModelData(
    val model: String,
    val sessionCreatePrice: Double,
    val tokenMultiplier: Double,
    val allowPreset: Boolean
)
