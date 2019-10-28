package com.tufin.document

import com.tufin.webhook.model.WebHook.EventType
import com.tufin.webhook.model.WebHook
import java.util.*

data class WebHookDocument(
        var uid: String,
        var name: String,
        var eventType: EventType,
        var url: String
) {

    constructor() : this("", "", EventType.ALL, "")

    constructor(webHook: WebHook) : this(
            webHook.uid,
            webHook.name,
            webHook.eventType,
            webHook.url
    )

    fun toWebHook() = WebHook(
            this.uid,
            this.name,
            this.eventType,
            this.url
    )
}