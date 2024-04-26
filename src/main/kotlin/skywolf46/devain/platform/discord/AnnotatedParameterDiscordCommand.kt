package skywolf46.devain.platform.discord

import arrow.core.Option
import arrow.core.none
import arrow.core.toOption
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import skywolf46.devain.annotations.CommandParameter
import skywolf46.devain.annotations.Required
import skywolf46.devain.util.replaceAllArgument
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.valueParameters

abstract class AnnotatedParameterDiscordCommand<T : Any>(
    command: String, val parameterClass: KClass<T>, descrption: String = "제공된 명령어 설명이 존재하지 않습니다."
) : ImprovedDiscordCommand(command, descrption) {
    private val parsed = ParsedParameter(parameterClass)

    override fun modifyCommandData(options: SlashCommandData) {
        for ((_, data) in parsed.parameters) {
            options.addOption(data.type.option, data.name, data.description.replaceAllArgument(onCommandParameterDataRequested()), data.required)
        }
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        val constructed = parameterClass.constructors.first().callBy(
            parsed.parameters.mapValues {
                it.value.type.converter(event, it.value.name)
            }.filterValues { it != null }
        )
        onParameterCommand(CommandEvent(event), constructed)
    }

    abstract suspend fun onParameterCommand(event: CommandEvent, data: T)

    open fun onCommandParameterDataRequested() : Map<String, String> = emptyMap()

    private class ParsedParameter(private val cls: KClass<*>) {
        val parameters = mutableMapOf<KParameter, ParsedParameterInfo>()

        init {
            verify().tap {
                throw RuntimeException(it)
            }
            inspect()
        }

        fun verify(): Option<String> {
            for (field in cls.constructors.first().valueParameters) {
                if (!field.isOptional) {
                    if (field.findAnnotations(CommandParameter::class).isEmpty()) {
                        return "Cannot use field \"${field.name}\" as parameter constructor : Non-optional field must have @Require and @CommandParameter annotation".toOption()
                    }
                    if (field.findAnnotations(Required::class).isEmpty()) {
                        return "Cannot use field \"${field.name}\" as parameter constructor : Non-optional field must have @Require and @CommandParameter annotation".toOption()
                    }
                }
            }
            return none()
        }

        fun inspect() {
            for (field in cls.constructors.first().valueParameters) {
                val data = field.findAnnotations(CommandParameter::class).firstOrNull()
                if (data != null) {
                    parameters[field] = ParsedParameterInfo(
                        field.findAnnotations(Required::class).isNotEmpty(), when (field.type.classifier as KClass<*>) {
                            Long::class -> OptionClassConverter.LONG
                            Double::class -> OptionClassConverter.DOUBLE
                            Float::class -> OptionClassConverter.FLOAT
                            String::class -> OptionClassConverter.STRING
                            Boolean::class -> OptionClassConverter.BOOLEAN
                            Int::class -> OptionClassConverter.INTEGER
                            else -> throw IllegalStateException("Class ${field.type.classifier} is unexpected for command parameter")
                        }, data.name, data.description
                    )
                }
            }
        }

        data class ParsedParameterInfo(
            val required: Boolean, val type: OptionClassConverter, val name: String, val description: String
        )
    }

    enum class OptionClassConverter(
        val option: OptionType, val converter: SlashCommandInteractionEvent.(String) -> Any?
    ) {
        STRING(OptionType.STRING, {
            getOption(it)?.asString
        }),
        LONG(OptionType.INTEGER, {
            getOption(it)?.asLong
        }),
        BOOLEAN(OptionType.BOOLEAN, {
            getOption(it)?.asBoolean
        }),
        DOUBLE(OptionType.NUMBER, {
            getOption(it)?.asDouble
        }),
        FLOAT(OptionType.NUMBER, {
            getOption(it)?.asDouble?.toFloat()
        }),
        INTEGER(OptionType.INTEGER, {
            getOption(it)?.asInt?.toFloat()
        })
    }

    data class CommandEvent(val origin: SlashCommandInteractionEvent, val executedOn: Long = System.currentTimeMillis()) {
        fun elapsed() : Long = (System.currentTimeMillis() - executedOn)
    }
}