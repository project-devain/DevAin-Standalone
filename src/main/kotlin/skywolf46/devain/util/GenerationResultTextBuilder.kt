package skywolf46.devain.util

import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color

class GenerationResultTextBuilder(val title: String, val content: String?, val metadata: List<Map<String, String>>) {
    fun asEmbed(builder: EmbedBuilder) {
        builder.setTitle(title)
        builder.setColor(Color(162, 103, 181))
        if (content != null) {
            builder.setDescription(content)
        }
        for (line in metadata) {
            if (line.size == 1) {
                val field = line.entries.first()
                builder.addField(field.key, field.value, false)
            } else {
                for ((k, v) in line) {
                    builder.addField(k, v, true)
                }
            }
        }
    }

}