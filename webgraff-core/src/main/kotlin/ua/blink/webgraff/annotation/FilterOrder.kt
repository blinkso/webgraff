package ua.blink.webgraff.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class FilterOrder(val value: Int)