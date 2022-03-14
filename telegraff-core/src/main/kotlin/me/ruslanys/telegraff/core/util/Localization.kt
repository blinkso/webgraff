package me.ruslanys.telegraff.core.util

import java.util.*

fun String.localized(
    locale: Locale? = null
): String {
    return runCatching {
        ResourceBundle.getBundle(
            "strings",
            locale ?: DEFAULT_LOCALE,
            UTF8Control()
        ).getString(this)
    }.getOrNull() ?: String.EMPTY
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
            Locale(ENGLISH_LOCALE),
            UTF8Control()
        ).getString(this),
        ResourceBundle.getBundle(
            "strings",
            Locale(UKRAINIAN_LOCALE),
            UTF8Control()
        ).getString(this)
    )
}

const val RUSSIAN_LOCALE = "ru"
const val UKRAINIAN_LOCALE = "uk"
const val ENGLISH_LOCALE = "en"
val DEFAULT_LOCALE = Locale(UKRAINIAN_LOCALE)