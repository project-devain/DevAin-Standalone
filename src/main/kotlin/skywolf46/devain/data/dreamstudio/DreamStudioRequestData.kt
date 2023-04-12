package skywolf46.devain.data.dreamstudio

class DreamStudioRequestData(
    val prompt : List<Pair<String, Double>>,
    val cfgScale: Double = 7.0,
    val steps: Int = 40,
    val clip_guidance_preset: String = "FAST_BLUE",
    val height: Int = 512,
    val width: Int = 512,
) {
}