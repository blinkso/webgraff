package ua.blink.webgraff.dto

/**
 * Response class for web chat token generation requests.
 *
 * @property token The JWT token to be used by the client for authentication
 * @property identity The identity associated with the token
 * @property serviceSid The Twilio Conversations service SID
 */
data class WebChatTokenResponse(
    val token: String,
    val identity: String,
    val serviceSid: String
) 