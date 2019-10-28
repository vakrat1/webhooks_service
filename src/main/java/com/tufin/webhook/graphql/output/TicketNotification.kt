package com.tufin.webhook.graphql.output

import com.tufin.webhook.model.WebHook
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TicketNotification(val ticketId: String,
                         val dateTime: String,
                         val eventType: WebHook.EventType,
                         val payload: String) {
//                         val payload: Map<String, Any>) {

//    val dateTime: String
//
//    init {
//        this.dateTime = dateTime.format(DateTimeFormatter.ISO_DATE_TIME)
//    }
}
