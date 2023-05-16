package ua.blink.telegraff.util

import org.springframework.core.annotation.AnnotationUtils

object TelegramFilterOrderUtil {
    fun getOrder(type: Class<*>): Int {
        return AnnotationUtils.findAnnotation(
            type,
            ua.blink.telegraff.annotation.TelegramFilterOrder::class.java
        )?.value ?: 0
    }
}