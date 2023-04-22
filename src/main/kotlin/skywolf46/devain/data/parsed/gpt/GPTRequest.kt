package skywolf46.devain.data.parsed.gpt

data class GPTRequest(val model: String, val contents: String, val temperature: Double, val top_p: Double, val maxToken : Int = -1, val hideRequest: Boolean = false, val timeStamp: Long = System.currentTimeMillis())