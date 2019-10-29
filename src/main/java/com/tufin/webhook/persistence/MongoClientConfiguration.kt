package com.tufin.webhook.persistence

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import mu.KotlinLogging
import net.jodah.failsafe.Failsafe
import net.jodah.failsafe.RetryPolicy
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.PreDestroy

private val logger = KotlinLogging.logger {}

@Configuration
class MongoClientConfiguration(
    @Value("\${mongodb.host:localhost}") private val host: String,
    @Value("\${mongodb.port:27017}") private val port: Int,
    @Value("\${mongodb.database:webhooks_service_db}") private val database: String,
    @Value("\${mongodb.maxConnections:10}") private val maxConnections: Int/*,
    @Value("\${mongodb.username:yaniv}") private val username: String,
    @Value("\${mongodb.password:yaniv}") private val password: String*/
) {

    private val executorService = Executors.newScheduledThreadPool(
        5, ThreadFactoryBuilder().setNameFormat("WEBHOOK_INIT-%s").build()
    )
    private val mongoDatabase: MongoDatabase by lazy { createDatabase().block()!! }

    fun getDatabase(): MongoDatabase {
        return mongoDatabase
    }

    fun getCollection(collectionName: String): MongoCollection<Document> =
        getDatabase().getCollection(collectionName)

    fun <D> getCollection(collectionName: String, clazz: Class<D>): MongoCollection<D> =
        getDatabase().getCollection(collectionName, clazz)

    private fun createDatabase(): Mono<MongoDatabase> {
        return Failsafe.with<Any>(
            RetryPolicy().withBackoff(1, 5, TimeUnit.SECONDS)
        )
            .with(executorService)
            .onFailedAttempt { _, _, executionContext ->
                logger.warn { "Failed attempt #${executionContext.executions} to init  mongo DB, retrying" }
            }
            .onFailure { _, throwable, executionContext ->
                logger.error(throwable) { "Failed to initialize mongo DB after ${executionContext.executions} attempts" }
            }
            .onSuccess { _, executionContext ->
                logger.info { "Initialized mongo DB after ${executionContext.executions} attempts, took ${executionContext.elapsedTime.toMillis()} ms." }
            }
            .future(
                Callable {
                    Mono.just(getClient().getDatabase(database))
                        .flatMap {
                            DBInitializer.initDatabase(it)
                                .thenReturn(it)
                        }.toFuture()
                }
            ).toMono()
    }

    private fun getClient(): MongoClient {
        val settings: MongoClientSettings = MongoClientSettings
                .builder()
                .applyToConnectionPoolSettings { it.maxSize(maxConnections) }
                .applyToClusterSettings { it.hosts(listOf(ServerAddress(host, port))) }
                .codecRegistry(
                CodecRegistries.fromRegistries(
                        MongoClients.getDefaultCodecRegistry(),
                        CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
                    )
                )
//                .credential(MongoCredential.createScramSha1Credential(username, database, password.toCharArray()))
                .build()

        val mongoClient = MongoClients.create(settings)
        logger.info { "Mongo client created for $host:$port/$database (maxConnections: $maxConnections)." }
        return mongoClient
    }

    @PreDestroy
    fun shutdown() {
        logger.info { "Closing down Mongo client." }
        getClient().close()
    }
}