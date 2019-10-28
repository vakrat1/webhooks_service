package com.tufin.webhook.graphql.input

import com.tufin.webhook.model.WebHook
import com.tufin.webhook.model.WebHook.EventType
import java.util.UUID.randomUUID

data class WebHookInput(var name: String, var eventType: EventType, var url: String) {

//    constructor() : this(EventType.ALL, "")

    fun toWebHook(uid: String) = WebHook (uid, this.name, this.eventType, this.url)
}

