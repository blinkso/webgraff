package ua.blink.telegraff.client

import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationListener
import ua.blink.telegraff.component.TelegramApi
import ua.blink.telegraff.dto.TelegramUpdate
import ua.blink.telegraff.event.TelegramUpdateEvent
import java.util.concurrent.TimeUnit

class TelegramPollingClient(
    private val telegramApi: TelegramApi,
    private val publisher: ApplicationEventPublisher
) : ua.blink.telegraff.client.TelegramClient, ApplicationListener<ApplicationReadyEvent> {

    private val client = Client()

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        start()
    }

    override fun start() {
        ua.blink.telegraff.client.TelegramPollingClient.Companion.log.info("Telegram client: POLLING")
        client.start()
    }

    @PreDestroy
    override fun shutdown() {
        client.interrupt()
        client.join()
    }

    override fun onUpdate(update: TelegramUpdate) {
        ua.blink.telegraff.client.TelegramPollingClient.Companion.log.info("Got a new event: {}", update)
        publisher.publishEvent(TelegramUpdateEvent(this, update))
    }

    private inner class Client : Thread("PollingClient") {

        private var offset: Long = 0

        override fun run() {
            while (!isInterrupted) {
                try {
                    val updates = telegramApi.getUpdates(
                        offset,
                        ua.blink.telegraff.client.TelegramPollingClient.Companion.POLLING_TIMEOUT
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
                    ua.blink.telegraff.client.TelegramPollingClient.Companion.log.error(
                        "Exception during polling messages",
                        e
                    )
                    TimeUnit.SECONDS.sleep(10)
                }
            }
        }

    }

    companion object {
        private val log = LoggerFactory.getLogger(ua.blink.telegraff.client.TelegramPollingClient::class.java)
        private const val POLLING_TIMEOUT = 10
    }


}