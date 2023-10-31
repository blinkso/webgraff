package ua.blink.whatsappgraff.client

import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationListener
import ua.blink.whatsappgraff.component.ConversationApi
import ua.blink.whatsappgraff.dto.Message
import ua.blink.whatsappgraff.event.UpdateEvent
import java.util.concurrent.TimeUnit

class PollingClient(
    private val conversationApi: ConversationApi,
    private val publisher: ApplicationEventPublisher
) : Client, ApplicationListener<ApplicationReadyEvent> {

    private val client = Client()

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        start()
    }

    override fun start() {
        log.info("WhatsApp client: POLLING")
        client.start()
    }

    @PreDestroy
    override fun shutdown() {
        client.interrupt()
        client.join()
    }

    override fun onUpdate(update: Message) {
        log.info("Got a new event: {}", update)
        publisher.publishEvent(UpdateEvent(this, update))
    }

    private inner class Client : Thread("PollingClient") {

        private var offset: String = ""

        override fun run() {
            while (!isInterrupted) {
                try {
                    val updates = conversationApi.getUpdates(
                        offset,
                        POLLING_TIMEOUT
                    )
                    if (updates.isEmpty()) {
                        continue
                    }

                    // publish updates
                    for (update in updates) {
                        onUpdate(update)
                    }

                    // --
                    offset = updates.last().sid
                } catch (e: Exception) {
                    log.error("Exception during polling messages", e)
                    TimeUnit.SECONDS.sleep(10)
                }
            }
        }

    }

    companion object {
        private val log = LoggerFactory.getLogger(PollingClient::class.java)
        private const val POLLING_TIMEOUT = 10
    }


}