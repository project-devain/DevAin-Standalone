package skywolf46.devain.model.api.openai.completion

data class OpenAIGPTPersonality(val gptName: String, val personality: MutableList<OpenAIGPTMessage>)