package com.tufin.webhook.persistence

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import com.tufin.webhook.persistence.DBCollectionField.WEBHOOK_UID
//import com.tufin.user.persistence.DBCollectionField.ROLE_UID
//import com.tufin.user.persistence.DBCollectionField.USER_USERNAME
import com.tufin.document.WebHookDocument
import mu.KotlinLogging
import org.bson.Document
import org.bson.conversions.Bson
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono

private val logger = KotlinLogging.logger {}

object DBInitializer {

    fun initDatabase(database: MongoDatabase): Mono<Void> {
        return database.listCollectionNames()
            .toFlux()
            .collectList()
            .flatMap { existingCollections ->
                DBCollection.values()
                    .filter { !existingCollections.contains(it.collectionName) }
                    .toFlux()
                    .doOnSubscribe { logger.info { "Start creating DB collections." } }
                    .doOnNext { logger.info { "Creating ${it.collectionName} collection." } }
                    .flatMap { it ->
                        database.createCollection(it.collectionName)
                            .toMono()
                            .then(Mono.defer { createIndexes(database.getCollection(it.collectionName), it.indexes) })
                    }
                    .then()
            }
    }

    private fun createIndexes(collection: MongoCollection<Document>, indexes: Set<Index>): Mono<Void> {
        return Flux.merge(
            indexes.map {
                collection.createIndex(it.key, IndexOptions().unique(it.isUnique))
                    .toMono()
            }
        )
            .doOnComplete { logger.info { "Mongo indexes for ${collection.namespace} collection created successfully." } }
            .doOnError { logger.error(it) { "Failed to create Mongo indexes for ${collection.namespace} collection." } }
            .then()
    }
}

enum class DBCollection(val collectionName: String, val indexes: Set<Index>) {
    WEBHOOKS(
            "webhooks",
            setOf(Index(Indexes.ascending(WEBHOOK_UID.fieldName), true))
    )
//    USERS(
//        "users",
//        setOf(Index(Indexes.ascending(USER_USERNAME.fieldName), true))
//    ),
//    ROLES(
//        "roles",
//        setOf(Index(ascending(ROLE_UID.fieldName), true), Index(Indexes.ascending(ROLE_NAME.fieldName), true))
//    )
}

enum class DBCollectionField(val fieldName: String) {
    WEBHOOK_UID(WebHookDocument::uid.name),
//    USER_UID(UserDocument::uid.name),
//    USER_USERNAME(UserDocument::userName.name),
//    USER_ROLE_UIDS(UserDocument::roleUids.name),
//    ROLE_UID(RoleDocument::uid.name),
//    ROLE_NAME(RoleDocument::name.name)
}

data class Index(val key: Bson, val isUnique: Boolean = false)