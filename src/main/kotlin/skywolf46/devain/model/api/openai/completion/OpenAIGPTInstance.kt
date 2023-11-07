package skywolf46.devain.model.api.openai.completion

data class OpenAIGPTInstance(val personality: OpenAIGPTPersonality, val prompts : MutableList<OpenAIGPTMessage> = mutableListOf())