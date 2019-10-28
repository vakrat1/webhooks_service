package com.tufin.webhook.graphql.publishers

import com.tufin.webhook.graphql.output.StockPriceUpdate

import io.nats.client.Nats
import io.nats.client.Options

import mu.KotlinLogging
import reactor.core.publisher.Flux
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink

import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.ArrayList
import java.util.Random
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private val LOGGER = KotlinLogging.logger {}

@Component
class StockTickerPublisher {

    val publisher: Flux<StockPriceUpdate>

//    val natsConnection:

    init {
//        initConnection()

        val stockPriceUpdatePublisher = Flux.create<StockPriceUpdate> { emitter ->

            val executorService = Executors.newScheduledThreadPool(1)
            executorService.scheduleAtFixedRate(newStockTick(emitter), 0, 2, TimeUnit.SECONDS)
        }

        val connectableFlux = stockPriceUpdatePublisher.share().publish()
        publisher = connectableFlux.autoConnect()

        //        publisher = connectableFlux.to(BackpressureStrategy.BUFFER);
    }

    private fun initConnection() {
        val o = Options.Builder()
                .server("nats://localhost:4222")
                .reconnectBufferSize(300)
                .build()
        val nc = Nats.connect(o)
        val d = nc.createDispatcher { msg ->
            System.out.printf("Received message \"%s\" on subject \"%s\"\n",
                    String(msg.data, StandardCharsets.UTF_8),
                    msg.subject)
        }
        d.subscribe("yanivnats")
    }

    private fun newStockTick(emitter: FluxSink<StockPriceUpdate>): Runnable {
        return Runnable {
            val stockPriceUpdates = getUpdates(rollDice(0, 5))
            emitStocks(emitter, stockPriceUpdates)
        }
    }

    private fun emitStocks(emitter: FluxSink<StockPriceUpdate>, stockPriceUpdates: List<StockPriceUpdate>) {
        for (stockPriceUpdate in stockPriceUpdates) {
            try {
                emitter.next(stockPriceUpdate)
            } catch (e: RuntimeException) {
                LOG.error("Cannot send StockUpdate", e)
            }

        }
    }

    fun getPublisher(stockCodes: List<String>?): Flux<StockPriceUpdate> {
        return if (stockCodes != null) {
            publisher.filter { stockPriceUpdate -> stockCodes.contains(stockPriceUpdate.stockCode) }
        } else publisher
    }

    private fun getUpdates(number: Int): List<StockPriceUpdate> {
        val updates = ArrayList<StockPriceUpdate>()
        for (i in 0 until number) {
            updates.add(rollUpdate())
        }
        return updates
    }

    private fun rollUpdate(): StockPriceUpdate {
        val STOCK_CODES = ArrayList(CURRENT_STOCK_PRICES.keys)

        val stockCode = STOCK_CODES[rollDice(0, STOCK_CODES.size - 1)]
        val currentPrice = CURRENT_STOCK_PRICES[stockCode] ?: error("Failed to retrieve stock: " + stockCode)


        var incrementDollars = dollars(rollDice(0, 1), rollDice(0, 99))
        if (rollDice(0, 10) > 7) {
            // 0.3 of the time go down
            incrementDollars = incrementDollars.negate()
        }
        val newPrice = currentPrice.add(incrementDollars)

        CURRENT_STOCK_PRICES[stockCode] = newPrice
        return StockPriceUpdate(stockCode, LocalDateTime.now(), newPrice, incrementDollars)
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(TicketNotificationPublisher::class.java)


        private val CURRENT_STOCK_PRICES = mutableMapOf<String, BigDecimal>()

        init {
            CURRENT_STOCK_PRICES["TEAM"] = dollars(39, 64)
            CURRENT_STOCK_PRICES["IBM"] = dollars(147, 10)
            CURRENT_STOCK_PRICES["AMZN"] = dollars(1002, 94)
            CURRENT_STOCK_PRICES["MSFT"] = dollars(77, 49)
            CURRENT_STOCK_PRICES["GOOGL"] = dollars(1007, 87)
        }

        private fun dollars(dollars: Int, cents: Int): BigDecimal {
            return truncate("$dollars.$cents")
        }

        private fun truncate(text: String): BigDecimal {
            var bigDecimal = BigDecimal(text)
            if (bigDecimal.scale() > 2)
                bigDecimal = BigDecimal(text).setScale(2, RoundingMode.HALF_UP)
            return bigDecimal.stripTrailingZeros()
        }

        private val rand = Random()

        private fun rollDice(min: Int, max: Int): Int {
            return rand.nextInt(max - min + 1) + min
        }
    }

}
