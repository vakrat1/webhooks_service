package com.tufin.webhook.graphql.output

import com.tufin.webhook.model.WebHook
import reactor.core.publisher.Mono

data class WebHookOutput(var id: String = "", val name: String, val url: String){
    companion object {
        fun fromWebHook(webHook: WebHook) = WebHookOutput(
                webHook.uid,
                webHook.name,
                webHook.url
        )
    }
}