package ua.blink.webgraff.filter

interface FiltersFactory {
    fun getFilters(): List<Filter>
}