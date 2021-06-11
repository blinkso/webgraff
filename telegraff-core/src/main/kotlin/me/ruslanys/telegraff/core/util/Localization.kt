package me.ruslanys.telegraff.core.util

import java.util.*

fun String.localized(
    locale: Locale? = null
): String {
    val resourceBundle = ResourceBundle.getBundle(
        "strings",
        locale ?: DEFAULT_LOCALE,
        UTF8Control()
    )
    return resourceBundle.getString(this)
}

/**
 * Should be extended every time we add new language to the system
 */
fun String.allLocalizedValues(): Set<String> {
    return setOf(
        ResourceBundle.getBundle(
            "strings",
            DEFAULT_LOCALE,
            UTF8Control()
        ).getString(this),
        ResourceBundle.getBundle(
            "strings",
            Locale("en"),
            UTF8Control()
        ).getString(this),
        ResourceBundle.getBundle(
            "strings",
            Locale("uk"),
            UTF8Control()
        ).getString(this)
    )
}

val DEFAULT_LOCALE = Locale("ru")