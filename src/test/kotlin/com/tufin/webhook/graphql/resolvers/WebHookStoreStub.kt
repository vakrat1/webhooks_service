package com.tufin.webhook.graphql.resolvers

import com.google.common.base.Stopwatch
import com.mongodb.client.model.Filters
import com.tufin.document.WebHookDocument
import com.tufin.webhook.exceptions.PersistenceException
import com.tufin.webhook.model.WebHook
import com.tufin.webhook.persistence.DBCollectionField
import com.tufin.webhook.persistence.LOG_LEVEL
import com.tufin.webhook.persistence.WebHookStore
import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import reactor.core.scheduler.Schedulers


private val logger = KotlinLogging.logger {}

class WebHookStoreStub(private var mock_db: MutableMap<String, WebHookDocument>) : WebHookStore {


    constructor() : this(mutableMapOf<String, WebHookDocument>()) 


    private val persistenceScheduler = Schedulers.newSingle("WebHookStore")

    fun MutableMap<String, WebHookDocument>.countDocuments() : Long{
        return mock_db.size.toLong()
    }

    fun MutableMap<String, WebHookDocument>.insertOne(webhookaDoc : WebHookDocument) {
        this[webhookaDoc.uid] = webhookaDoc
    }

    fun MutableMap<String, WebHookDocument>.find(fieldToFilterBy: Map<String, Any>) : WebHookDocument{
        return fieldToFilterBy.values.map { mock_db[it]}.filterNotNull().get(0)
    }

    fun MutableMap<String, WebHookDocument>.find() : List<WebHookDocument>{
        return mock_db.values.toList()
    }

    fun MutableMap<String, WebHookDocument>.replaceOne(
            fieldToFilterBy: Map<String, Any>, webhookaDoc : WebHookDocument) : Boolean{
        val exists = mock_db.containsKey(webhookaDoc.uid)
        when (exists) {
            true -> mock_db[webhookaDoc.uid] =  webhookaDoc
        }
        return exists
    }

    fun MutableMap<String, WebHookDocument>.deleteOne(fieldToFilterBy: Map<String, Any>) : Boolean{
        val doc = fieldToFilterBy.values.elementAt(0)
        val exists = mock_db.containsKey(doc)
        when (exists) {
            true -> mock_db.remove(doc)
        }
        return exists
    }

    fun toDoc(webhook: WebHook): WebHookDocument {
        return WebHookDocument(webhook)
    }

    fun fromDoc(doc: WebHookDocument): WebHook{
        return WebHook(doc.uid, doc.name, doc.eventType, doc.url)
    }

    private fun getWebHooksCollection(): MutableMap<String, WebHookDocument> {
        return mock_db
    }

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
                .replaceOne(eqFilter(DBCollectionField.WEBHOOK_UID, uid), WebHookDocument(webHook))
                .toMono()
                .addLogging("update webhook uid: $uid" + ", url: " + webHook.url, LOG_LEVEL.INFO)
    }

    override fun deleteWebHook(uid: String): Mono<Boolean> {
        return getWebHooksCollection()
                .deleteOne(eqFilter(DBCollectionField.WEBHOOK_UID, uid))
                .toMono()
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


    private fun eqFilter(field: DBCollectionField, value: String) = mapOf<String, Any>(field.fieldName to value)

}