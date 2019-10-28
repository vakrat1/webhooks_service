package com.tufin.webhook.graphql.publishers

import com.tufin.webhook.graphql.output.TicketNotification
import com.tufin.webhook.model.WebHook
import io.nats.client.Connection
import io.nats.client.Nats
import io.nats.client.Options
import mu.KotlinLogging
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val LOGGER = KotlinLogging.logger {}

@Component
class TicketNotificationPublisher {

    val publisher: Flux<TicketNotification>

//    val natsConnection:

    init {

//        lateinit var _emitter: FluxSink<TicketNotification>

        val ticketNotificationPublisher = Flux.create<TicketNotification> { emitter ->
//            _emitter = emitter
//            val executorService = Executors.newScheduledThreadPool(1)
//            executorService.scheduleAtFixedRate(newStockTick(emitter), 0, 2, TimeUnit.SECONDS
            val nc = initConnection()

            val d = nc.createDispatcher { msg ->
                System.out.printf("Received message \"%s\" on subject \"%s\"\n",
                        String(msg.data, StandardCharsets.UTF_8),
                        msg.subject)
                //TODO - subscribe on the relevant notifications topics in NATS (CREATED, UPDATED, ... or any other event type
                //TODO - it might be that it will be single topic( subject) for all updates, and the filter will be based on the Event Type
                //TODO - in any case, based on the notification event, we should notify all the susbscribers of the new event
                //TODO - should we use WS channel, or should we use HTTP POST request
                sendNotification(emitter, TicketNotification("1234",
                        LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), WebHook.EventType.CREATED, ""))
            }

            d.subscribe("yanivnats")
        }




        val connectableFlux = ticketNotificationPublisher.share().publish()
        publisher = connectableFlux.autoConnect()

        //        publisher = connectableFlux.to(BackpressureStrategy.BUFFER);
    }

    private fun initConnection(): Connection {
        val o = Options.Builder()
                .server("nats://localhost:4222")
                .reconnectBufferSize(300)
                .build()
        return Nats.connect(o)
    }

//    private fun newStockTick(emitter: FluxSink<TicketNotification>): Runnable {
//        return Runnable {
//            val stockPriceUpdates = getUpdates(rollDice(0, 5))
//            emitStocks(emitter, stockPriceUpdates)
//        }
//    }

//    private fun emitStocks(emitter: FluxSink<TicketNotification>, stockPriceUpdates: List<TicketNotification>) {
//        for (stockPriceUpdate in stockPriceUpdates) {
//            try {
//                emitter.next(stockPriceUpdate)
//            } catch (e: RuntimeException) {
//                LOG.error("Cannot send StockUpdate", e)
//            }
//
//        }
//    }

    private fun sendNotification(emitter: FluxSink<TicketNotification>, ticketNotification: TicketNotification) {
        try {
            emitter.next(ticketNotification)
        } catch (e: RuntimeException) {
            LOGGER.error("Cannot send Ticket Update", e)
        }
    }

//    fun getPublisher(): Flux<TicketNotification> {
//        return publisher
//    }

//    private fun getUpdates(number: Int): List<TicketNotification> {
//        val updates = ArrayList<TicketNotification>()
//        for (i in 0 until number) {
//            updates.add(rollUpdate())
//        }
//        return updates
//    }
//
//    private fun rollUpdate(): TicketNotification {
//        val STOCK_CODES = ArrayList(CURRENT_STOCK_PRICES.keys)
//
//        val stockCode = STOCK_CODES[rollDice(0, STOCK_CODES.size - 1)]
//        val currentPrice = CURRENT_STOCK_PRICES[stockCode] ?: error("Failed to retrieve stock: " + stockCode)
//
//
//        var incrementDollars = dollars(rollDice(0, 1), rollDice(0, 99))
//        if (rollDice(0, 10) > 7) {
//            // 0.3 of the time go down
//            incrementDollars = incrementDollars.negate()
//        }
//        val newPrice = currentPrice.add(incrementDollars)
//
//        CURRENT_STOCK_PRICES[stockCode] = newPrice
//        return TicketNotification(stockCode, LocalDateTime.now(), WebHook.EventType.CREATED, mapOf())
//    }
//
//    companion object {
//
//        private val LOG = LoggerFactory.getLogger(TicketNotificationPublisher::class.java)
//
//
//        private val CURRENT_STOCK_PRICES = mutableMapOf<String, BigDecimal>()
//
//        init {
//            CURRENT_STOCK_PRICES["TEAM"] = dollars(39, 64)
//            CURRENT_STOCK_PRICES["IBM"] = dollars(147, 10)
//            CURRENT_STOCK_PRICES["AMZN"] = dollars(1002, 94)
//            CURRENT_STOCK_PRICES["MSFT"] = dollars(77, 49)
//            CURRENT_STOCK_PRICES["GOOGL"] = dollars(1007, 87)
//        }
//
//        private fun dollars(dollars: Int, cents: Int): BigDecimal {
//            return truncate("$dollars.$cents")
//        }
//
//        private fun truncate(text: String): BigDecimal {
//            var bigDecimal = BigDecimal(text)
//            if (bigDecimal.scale() > 2)
//                bigDecimal = BigDecimal(text).setScale(2, RoundingMode.HALF_UP)
//            return bigDecimal.stripTrailingZeros()
//        }
//
//        private val rand = Random()
//
//        private fun rollDice(min: Int, max: Int): Int {
//            return rand.nextInt(max - min + 1) + min
//        }
//    }

}
