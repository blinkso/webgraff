package ua.blink.webgraff.dto

/**
 * Request class for generating web chat tokens.
 *
 * @property identity The unique identifier for the user requesting the token
 * @property metadata Optional metadata associated with the token request
 */
data class WebChatTokenRequest(
    val identity: String,
    val metadata: Map<String, String>? = null
) 