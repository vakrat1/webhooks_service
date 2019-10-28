package com.tufin.webhook.graphql.output

import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class StockPriceUpdate(val stockCode: String,
                       dateTime: LocalDateTime,
                       val stockPrice: BigDecimal,
                       val stockPriceChange: BigDecimal) {

    val dateTime: String

    init {
        this.dateTime = dateTime.format(DateTimeFormatter.ISO_DATE_TIME)
    }
}
