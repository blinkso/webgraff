package ua.blink.whatsappgraff.filter

interface FiltersFactory {
    fun getFilters(): List<Filter>
}