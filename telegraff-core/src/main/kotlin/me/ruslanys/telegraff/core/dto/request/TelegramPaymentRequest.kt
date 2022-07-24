package me.ruslanys.telegraff.core.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import me.ruslanys.telegraff.core.dto.request.keyboard.TelegramRemoveReplyKeyboard

// https://core.telegram.org/bots/api#payments
open class TelegramPaymentRequest(
    chatId: Long = 0,

    @get:JsonProperty("title")
    val title: String,

    @get:JsonProperty("description")
    val description: String,

    @get:JsonProperty("description")
    val payload: String,

    @get:JsonProperty("provider_token")
    val providerToken: String,

    @get:JsonProperty("currency")
    val currency: String,

    @get:JsonProperty("prices")
    val prices: List<TelegramLabeledPrice>,

    @get:JsonProperty("need_phone_number")
    val needPhoneNumber: Boolean = false,

    @get:JsonProperty("need_email")
    val needEmail: Boolean = false,

    @get:JsonProperty("need_shipping_address")
    val needShippingAddress: Boolean = false,

    @get:JsonProperty("send_phone_number_to_provider")
    val sendPhoneNumberToProvider: Boolean = needPhoneNumber,

    @get:JsonProperty("send_email_to_provider")
    val sendEmailNumberToProvider: Boolean = needEmail,

    @get:JsonProperty("is_flexible")
    val isFlexible: Boolean = false
) : TelegramSendRequest(chatId, TelegramRemoveReplyKeyboard()) {
}