package com.tufin.webhook.persistence


import com.google.common.base.Stopwatch
import com.mongodb.client.model.Filters
import com.tufin.document.WebHookDocument
import com.tufin.webhook.exceptions.PersistenceException
import com.tufin.webhook.model.WebHook
import com.tufin.webhook.persistence.DBCollection.WEBHOOKS
import com.tufin.webhook.persistence.DBCollectionField.WEBHOOK_UID
import mu.KotlinLogging
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono

private val logger = KotlinLogging.logger {}

enum class LOG_LEVEL() {
    INFO,
    DEBUG,
    WARNING,
    ERROR
}

@Repository
class MongoWebHookStore(private val mongoClientConfiguration: MongoClientConfiguration) : WebHookStore {


    private fun getWebHooksCollection() =
            mongoClientConfiguration.getCollection(WEBHOOKS.collectionName, WebHookDocument::class.java)

    override fun countWebHooks(): Mono<Long> {
        return getWebHooksCollection()
                .countDocuments()
                .toMono()
                .addLogging("count webhooks")
    }


    override fun findWebHook(uid: String): Mono<WebHook> {
        return getWebHooksCollection()
                .find(eqFilter(DBCollectionField.WEBHOOK_UID, uid))
                .toMono()
                .map { it.toWebHook() }
                .addLogging("find webhook $uid")
    }

    override fun findAllWebHooks(): Flux<WebHook> {
        return getWebHooksCollection()
                .find()
                .toFlux()
                .map { it.toWebHook() }
//                .addLogging("successfully fetch all records")
    }


    override fun insertWebHook(webHook: WebHook): Mono<Boolean> {
        return getWebHooksCollection()
                .insertOne(WebHookDocument(webHook))
                .toMono()
                .map { it != null }
                .addLogging("insert webhook ${webHook.name}/${webHook.url}", LOG_LEVEL.INFO)
//                .addLogging(getInsertWebHooksLog(webHookRegRequests), LOG_LEVEL.INFO)
    }

    override fun updateWebHook(uid: String, webHook: WebHook): Mono<Boolean> {
        return getWebHooksCollection()
                .replaceOne(eqFilter(WEBHOOK_UID, uid), WebHookDocument(webHook))
                .toMono()
                .map { it.modifiedCount > 0 }
                .addLogging("update webhook uid: $uid" + ", url: " + webHook.url, LOG_LEVEL.INFO)
    }

    override fun deleteWebHook(uid: String): Mono<Boolean> {
        return getWebHooksCollection()
                .deleteOne(eqFilter(WEBHOOK_UID, uid))
                .toMono()
                .map { it.deletedCount > 0 }
                .addLogging("delete webhook uid: $uid", LOG_LEVEL.INFO)
    }

    private fun <T> Mono<T>.addLogging(action: String, log_level: LOG_LEVEL = LOG_LEVEL.DEBUG): Mono<T> {
        val stopwatch: Stopwatch = Stopwatch.createUnstarted()
        return this
                .doOnSubscribe { stopwatch.start() }
                .doOnSuccess {
                    logger.debug { "Done $action, took $stopwatch" }
                }
                .onErrorMap { PersistenceException("Failed to $action", it) }
    }

    private fun eqFilter(field: DBCollectionField, value: String) = Filters.eq(field.fieldName, value)

}