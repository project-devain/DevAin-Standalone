package skywolf46.devain.annotations.config

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class MarkConfigElement(val name: String = "")