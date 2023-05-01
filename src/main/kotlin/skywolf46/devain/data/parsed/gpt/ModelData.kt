package skywolf46.devain.data.parsed.gpt

data class ModelData(val modelName: String, val promptPrice: Double, val responsePrice: Double = promptPrice)