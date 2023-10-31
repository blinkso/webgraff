package ua.blink.whatsappgraff.client

import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationListener
import ua.blink.whatsappgraff.component.ConversationApi
import ua.blink.whatsappgraff.dto.Update
import ua.blink.whatsappgraff.event.UpdateEvent
import java.util.concurrent.TimeUnit

class PollingClient(
    private val conversationApi: ConversationApi,
    private val publisher: ApplicationEventPublisher
) : ua.blink.whatsappgraff.client.Client, ApplicationListener<ApplicationReadyEvent> {

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

    override fun onUpdate(update: Update) {
        ua.blink.whatsappgraff.client.PollingClient.Companion.log.info("Got a new event: {}", update)
        publisher.publishEvent(UpdateEvent(this, update))
    }

    private inner class Client : Thread("PollingClient") {

        private var offset: Long = 0

        override fun run() {
            while (!isInterrupted) {
                try {
                    val updates = conversationApi.getUpdates(
                        offset,
                        ua.blink.whatsappgraff.client.PollingClient.Companion.POLLING_TIMEOUT
                    )
                    if (updates.isEmpty()) {
                        continue
                    }

                    // publish updates
                    for (update in updates) {
                        onUpdate(update)
                    }

                    // --
                    offset = updates.last().id + 1
                } catch (e: Exception) {
                    ua.blink.whatsappgraff.client.PollingClient.Companion.log.error(
                        "Exception during polling messages",
                        e
                    )
                    TimeUnit.SECONDS.sleep(10)
                }
            }
        }

    }

    companion object {
        private val log = LoggerFactory.getLogger(ua.blink.whatsappgraff.client.PollingClient::class.java)
        private const val POLLING_TIMEOUT = 10
    }


}