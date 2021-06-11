package me.ruslanys.telegraff.core.filter

// Replaced with worn local aware filters

//@TelegramFilterOrder(Integer.MAX_VALUE)
//class UnresolvedMessageFilter(private val telegramApi: TelegramApi): TelegramFilter {
//
//    override fun handleMessage(message: TelegramMessage, chain: TelegramFilterChain) {
//        if ("PRIVATE".equals(message.chat.type, true)) {
//            val request = TelegramMessageSendRequest(
//                    message.chat.id,
//                    "Извини, я тебя не понимаю",
//                    TelegramParseMode.MARKDOWN
//            )
//            telegramApi.sendMessage(request)
//        }
//    }
//
//}