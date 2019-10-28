//package com.yaniv.persistence
//
//import com.tufin.user.persistence.MongoWebHooksStore
////import com.tufin.utils.addLogs
////import com.tufin.version.ReportMetadata
////import com.tufin.version.persistence.ReportMetadataStore
////import com.tufin.version.persistence.mongoDB.ReportMetadataDoc
//import com.yaniv.document.WebHookDocument
//import com.yaniv.graphql.resolvers.WebHookRegRequest
//import mu.KotlinLogging
//import reactor.core.publisher.Flux
//import reactor.core.publisher.Mono
//import reactor.core.publisher.toFlux
//import reactor.core.publisher.toMono
//import reactor.core.scheduler.Schedulers
//
//
//private val log = KotlinLogging.logger {}
//
//class WebHooksStoreStub() : WebHooksStore {
//
//    private val mock_db: MutableMap<String, WebHookDocument> = mutableMapOf<String, WebHookDocument>()
//    private val persistenceScheduler = Schedulers.newSingle("ReportMetadataStore")
//
//    fun MutableMap<String, WebHookDocument>.insertOne(reportMetadataDoc : WebHookDocument) {
//        this[reportMetadataDoc.id] = reportMetadataDoc
//    }
//
//    fun MutableMap<String, WebHookDocument>.find(fieldToFilterBy: Map<String, Any>) : List<WebHookDocument>{
//        return fieldToFilterBy.values.map { mock_db[it]}.filterNotNull()
//    }
//
//    fun toDoc(webHookRegRequest: WebHookRegRequest): WebHookDocument {
//        return WebHookDocument(webHookRegRequest.eventType, webHookRegRequest.url)
//    }
//
//    fun fromDoc(doc: WebHookDocument): WebHookRegRequest{
//        return WebHookRegRequest(
//    }
//
//    private fun getCollection(): MutableMap<String, ReportMetadataDoc> {
//        return mock_db
//    }
//
//    override fun insert(metaData: ReportMetadata): Mono<Void> {
//        return getCollection().insertOne(toDoc(metaData))
//            .toMono().publishOn(persistenceScheduler).then()
//            .addLogs("Add new report metadata", "$metaData", log)
//    }
//
//    override fun set(reportId: String, fieldToValue: Map<String, Any>): Mono<Void> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun get(fieldToFilterBy: Map<String, Any>): Flux<ReportMetadata> {
//
//        return getCollection()
//            .find(fieldToFilterBy)
//            .toFlux()
//            .publishOn(persistenceScheduler)
//            .map { fromDoc(it) }
//    }
//
//    override fun getEarliestReadyReport(systemId: String): Mono<ReportMetadata> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun getAllSystemIdsWithReadyReports(): Flux<String> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun deleteBy(fieldName: String, fieldValue: String): Mono<Void> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//
//}