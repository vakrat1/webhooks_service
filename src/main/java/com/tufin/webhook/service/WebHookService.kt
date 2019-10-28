package com.tufin.webhook.service

import com.tufin.webhook.model.WebHook
import com.tufin.webhook.persistence.WebHookStore
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

private val LOGGER = KotlinLogging.logger {}

@Service
class WebHookService (private val mongoWebhookStore: WebHookStore){

    fun registerWebHook(webHook: WebHook) : Mono<Boolean> = mongoWebhookStore.insertWebHook(webHook)

    fun getWebHook(id: String) : Mono<WebHook> = mongoWebhookStore.findWebHook(id)

    fun getAllWebHooks():Flux<WebHook> = mongoWebhookStore.findAllWebHooks()

    fun updateWebHook(uid: String, webHook: WebHook): Mono<Boolean> {
        return mongoWebhookStore.updateWebHook(uid, webHook)
    }

    fun deleteWebhook(uid: String): Mono<Boolean> {
        return mongoWebhookStore.deleteWebHook(uid)
        //TODO should be sent notification (ChangeDataSet) ???
    }
}