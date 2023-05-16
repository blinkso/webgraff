package ua.blink.telegraff.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import ua.blink.telegraff.dto.request.keyboard.TelegramPaymentReplyKeyboard

// https://core.telegram.org/bots/api#sendinvoice
open class TelegramPaymentRequest(
    chatId: Long = 0,

    paymentButtonText: String,

    @get:JsonProperty("title")
    val title: String,

    @get:JsonProperty("description")
    val description: String,

    @get:JsonProperty("payload")
    val payload: String,

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
    val isFlexible: Boolean = false,

    @get:JsonProperty("provider_token")
    var providerToken: String = "",

    @get:JsonProperty("provider_data")
    var providerData: String = "",

    @get:JsonProperty("photo_url")
    val photo: String? = null,

    @get:JsonProperty("start_parameter")
    val startParameter: String = ""
) : TelegramSendRequest(chatId, TelegramPaymentReplyKeyboard(paymentButtonText)) {

    companion object {
        const val TITLE_MAX_LENGTH = 32
        const val DESCRIPTION_MAX_LENGTH = 255
    }
}