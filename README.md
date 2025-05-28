![Webgraff](docs/logo.png "Logo")

# Webgraff - Twilio Flex Web Chat Integration Framework

A Kotlin framework for integrating Twilio Flex web chat into your applications.

## Settings

```properties
# Required Twilio credentials
webchat.api-key-sid=              # Twilio API key SID 
webchat.api-key-secret=           # Twilio API key secret
webchat.account-sid=              # Twilio account SID
webchat.service-sid=              # Twilio Conversations service SID
webchat.flex-flow-sid=            # Twilio Flex flow SID

# Chat configuration
webchat.mode=                     # webhook (default), polling
webchat.webhook-base-url=         # required for webhook mode, e.g., https://your-domain.com
webchat.webhook-endpoint-url=     # optional, defaults to /webchat/{random-uuid}
webchat.token-ttl=                # JWT token validity in seconds, defaults to 3600 (1 hour)

# Optional UI templates
webchat.button-template=          # Optional button template ID for rich messages
webchat.list-template=            # Optional list template ID for rich messages
```

## Usage

Add `.kts` in resource `handlers` folder:
`resources/handlers/ExampleHandler.kts`.

```kotlin
enum class PaymentMethod {
    CARD, CASH
}

handler("/support") {
    step<String>("name") {
        question {
            MarkdownMessage("What's your name?")
        }
    }

    step<String>("issue") {
        question {
            MarkdownMessage("How can we help you today?")
        }
    }

    step<PaymentMethod>("paymentMethod") {
        question {
            MarkdownMessage("Which payment method do you use?", "Card", "Cash")
        }

        validation {
            when (it.toLowerCase()) {
                "Card" -> PaymentMethod.CARD
                "Cash" -> PaymentMethod.CASH
                else -> throw ValidationException("Please choose one of the options above")
            }
        }
    }

    process { state, answers ->
        val name = answers["name"] as String
        val issue = answers["issue"] as String
        val paymentMethod = answers["paymentMethod"] as PaymentMethod

        // Business logic

        MarkdownMessage("Thank you, $name. We're connecting you with an agent to help with your $paymentMethod payment issue.")
    }
}
```

## Web Client Integration

### Embedding Flex Web Chat Widget

Add the Twilio Flex Web Chat widget to your web page:

```html
<!-- Add Twilio Flex Web Chat to your page -->
<script src="https://assets.flex.twilio.com/releases/flex-webchat-ui/2.x.x/twilio-flex-webchat.min.js"></script>
<div id="flex-chat-container"></div>

<script>
  // Initialize chat when needed (e.g., when clicking a "Chat with us" button)
  function initializeChat() {
    // Get configuration from your server
    fetch('/api/chat-config?eventId=' + eventId)
      .then(response => response.json())
      .then(config => {
        // Initialize with FlexFlowSid (no need for direct token handling)
        Twilio.FlexWebChat.createWebChat({
          flexFlowSid: config.flexFlowSid,
          customerFriendlyName: 'Customer',
          context: config.context || {},
          preEngagementConfig: {
            description: "Need help buying tickets?",
            fields: [
              {
                label: "What's your name?",
                type: "InputItem",
                attributes: {
                  name: "friendlyName",
                  type: "text",
                  required: true
                }
              },
              {
                label: "What do you need help with?",
                type: "InputItem",
                attributes: {
                  name: "topic",
                  type: "text",
                  required: true
                }
              }
            ]
          }
        }).then(webchat => {
          // Render the widget
          webchat.init();
        });
      });
  }
  
  // Add a chat button to your page
  document.getElementById('chat-button').addEventListener('click', initializeChat);
</script>
```

### Ticket Sales Integration

For ticket sales flows, you can include event-specific context:

```javascript
// Get configuration with event details
fetch('/api/chat-config?eventId=EVENT123')
  .then(response => response.json())
  .then(config => {
    Twilio.FlexWebChat.createWebChat({
      flexFlowSid: config.flexFlowSid,
      customerFriendlyName: 'Customer',
      context: {
        eventId: config.context.eventId,
        eventName: "Summer Concert 2024",
        ticketType: "General Admission",
        source: "event_page"
      },
      // Other configuration...
    });
  });
```

## Error handling

![Error handling](docs/processing-diagram.png "Message processing")
