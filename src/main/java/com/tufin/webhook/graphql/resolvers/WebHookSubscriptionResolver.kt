package com.tufin.webhook.graphql.resolvers

import com.coxautodev.graphql.tools.GraphQLSubscriptionResolver
import com.tufin.webhook.graphql.output.StockPriceUpdate
import com.tufin.webhook.graphql.output.TicketNotification
import com.tufin.webhook.graphql.publishers.StockTickerPublisher
import com.tufin.webhook.graphql.publishers.TicketNotificationPublisher
import mu.KotlinLogging
import org.reactivestreams.Publisher
import org.springframework.stereotype.Component

private val LOGGER = KotlinLogging.logger {}

@Component
internal class WebHookSubscriptionResolver(private val stockTickerPublisher: StockTickerPublisher,
                                           private val ticketNotificationPublisher: TicketNotificationPublisher) : GraphQLSubscriptionResolver {

    fun stockQuotes(stockCodes: List<String>?): Publisher<StockPriceUpdate> {
        return stockTickerPublisher.getPublisher(stockCodes)
    }

    fun ticketUpdates(): Publisher<TicketNotification> {
        return ticketNotificationPublisher.publisher
    }

}
