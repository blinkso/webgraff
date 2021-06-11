package me.ruslanys.telegraff.core.filter

// Replaced with worn local aware filters

//@TelegramFilterOrder(0)
//class CancelFilter(private val telegramApi: TelegramApi, private val handlersFilter: HandlersFilter): TelegramFilter {
//
//    override fun handleMessage(message: TelegramMessage, chain: TelegramFilterChain) {
//        val text = message.text?.toLowerCase() ?: ""
//        if (text.startsWith("/cancel") || text.startsWith("отмена")) {
//            handlersFilter.clearState(message.chat)
//            telegramApi.sendMessage(MarkdownMessage("Хорошо, давай начнем сначала", chatId = message.chat.id))
//        } else {
//            chain.doFilter(message)
//        }
//    }
//
//}