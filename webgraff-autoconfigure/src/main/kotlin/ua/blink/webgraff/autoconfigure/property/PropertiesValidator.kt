package ua.blink.webgraff.autoconfigure.property

import org.springframework.validation.Errors
import org.springframework.validation.Validator

class PropertiesValidator : Validator {

    override fun supports(type: Class<*>): Boolean {
        return type === Properties::class.java
    }

    override fun validate(target: Any, errors: Errors) {
        val properties = target as Properties

        if (properties.apiKeySid.isEmpty()) {
            errors.rejectValue(
                "apiKeySid", "apiKeySid.null",
                "API Key SID must not be null!"
            )
        }

        if (properties.apiKeySecret.isEmpty()) {
            errors.rejectValue(
                "apiKeySecret", "apiKeySecret.null",
                "API Key Secret must not be null!"
            )
        }

        if (properties.accountSid.isEmpty()) {
            errors.rejectValue(
                "accountSid", "accountSid.null",
                "Account SID must not be null!"
            )
        }

        if (properties.serviceSid.isEmpty()) {
            errors.rejectValue(
                "serviceSid", "serviceSid.null",
                "Service SID must not be null!"
            )
        }

        if (properties.flexFlowSid.isEmpty()) {
            errors.rejectValue(
                "flexFlowSid", "flexFlowSid.null",
                "Flex Flow SID must not be null!"
            )
        }

        if (properties.handlersPath.isEmpty()) {
            errors.rejectValue(
                "handlersPath", "handlersPath.null",
                "Handlers path must not be null!"
            )
        }

        if (properties.mode == WebChatMode.WEBHOOK) {
            val webhookBaseUrl = properties.webhookBaseUrl
            if (webhookBaseUrl == null || webhookBaseUrl.isEmpty()) {
                errors.rejectValue(
                    "webhookBaseUrl", "webhookBaseUrl.empty",
                    "You have to set Webhook Base URL with Webhook mode."
                )
            } else if (!webhookBaseUrl.startsWith("https://")) {
                errors.rejectValue(
                    "webhookBaseUrl", "webhookBaseUrl.https",
                    "You have to set HTTPS protocol at Webhook base URL."
                )
            }
        }
    }

}