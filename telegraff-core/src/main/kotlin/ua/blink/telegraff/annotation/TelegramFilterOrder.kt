package ua.blink.telegraff.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class TelegramFilterOrder(val value: Int)