package ua.blink.whatsappgraff.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class FilterOrder(val value: Int)