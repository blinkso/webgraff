package ua.blink.telegraff.filter

interface TelegramFiltersFactory {
    fun getFilters(): List<TelegramFilter>
}