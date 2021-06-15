package me.ruslanys.telegraff.core.filter

class FilterOrders {

    companion object {
        const val CALLBACK_QUERY_ANSWER_FILTER_ORDER = -2
        const val DEEPLINK_FILTER_ORDER = -1
        const val CANCEL_FILTER_ORDER = 0
        const val HANDLERS_FILTER_ORDER = 1
        const val UNRESOLVED_FILTER_ORDER = Integer.MAX_VALUE
    }
}