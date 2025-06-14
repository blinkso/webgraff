![Webgraff](docs/logo.png "Logo")

# Webgraff - Twilio Web Chat Integration Framework

A Kotlin framework for integrating Twilio web chat into your applications.

## Settings

```properties
# Required Twilio credentials
webchat.api-key-sid=              # Twilio API key SID 
webchat.api-key-secret=           # Twilio API key secret
webchat.account-sid=              # Twilio account SID
webchat.service-sid=              # Twilio Conversations service SID

# Chat configuration
webchat.mode=                     # webhook (default), polling
webchat.webhook-base-url=         # required for webhook mode, e.g., https://your-domain.com
webchat.webhook-endpoint-url=     # optional, defaults to /webchat/{random-uuid}

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

### Embedding Web Chat on Your Site

```html
<div id="chat-container"></div>
<button id="start-chat">Chat with us</button>

<script src="https://sdk.twilio.com/js/conversations/2.0.0/twilio-conversations.min.js"></script>
<script>
  // Simple chat interface for your website
  document.getElementById('start-chat').addEventListener('click', async () => {
    // Step 1: Create a conversation
    const response = await fetch('/api/create-conversation?identity=user-' + Date.now());
    const data = await response.json();
    const conversationSid = data.conversationSid;
    
    // Initialize UI (you can use any chat UI library)
    const chatUI = document.getElementById('chat-container');
    chatUI.innerHTML = '<div id="messages"></div><input id="message-input"><button id="send">Send</button>';
    
    // Handle sending messages
    document.getElementById('send').addEventListener('click', () => {
      const message = document.getElementById('message-input').value;
      if (message) {
        // Send message to your server
        fetch(`/api/send-message?chatId=${conversationSid}&message=${encodeURIComponent(message)}`, {
          method: 'POST'
        });
        
        // Add message to UI
        addMessageToUI('user', message);
        document.getElementById('message-input').value = '';
      }
    });
    
    // Setup server-sent events or WebSocket to receive messages
    const eventSource = new EventSource(`/api/messages/${conversationSid}`);
    eventSource.onmessage = (event) => {
      const message = JSON.parse(event.data);
      addMessageToUI('bot', message.text);
    };
    
    function addMessageToUI(sender, text) {
      const messagesDiv = document.getElementById('messages');
      const messageElem = document.createElement('div');
      messageElem.className = `message ${sender}`;
      messageElem.textContent = text;
      messagesDiv.appendChild(messageElem);
      messagesDiv.scrollTop = messagesDiv.scrollHeight;
    }
  });
</script>
```

## Error handling

![Error handling](docs/processing-diagram.png "Message processing")
