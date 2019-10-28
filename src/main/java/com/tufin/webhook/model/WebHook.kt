package com.tufin.webhook.model

import java.util.UUID.randomUUID

data class WebHook(
        val uid: String = "",
        val name: String,
        val eventType: EventType,
        val url: String)
{
//        init {
//            if (this.uid.isNullOrEmpty()) {
//                uid = randomUUID().toString()
//            }
//        }
    enum class EventType { CREATED, UPDATED, DELETED, ALL }
}