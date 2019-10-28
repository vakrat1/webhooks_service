package com.tufin.webhook.graphql.resolvers

import com.tufin.document.WebHookDocument
import com.tufin.webhook.graphql.output.WebHookOutput
import com.tufin.webhook.model.WebHook
import com.tufin.webhook.persistence.WebHookStore
import com.tufin.webhook.service.WebHookService
import org.amshove.kluent.`should equal`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.endpoint.SecurityContext
import reactor.core.publisher.Mono
import java.util.*


import io.mockk.every
import io.mockk.mockk

class WebHookQueryResolverTest {


//    private val mockSecurityContext: SecurityContext = SecurityContextImpl()

    @BeforeEach
    fun init() {
//        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun getWebHook_returnWebHookFromStore() {

        val webHookId = "c7f99d9f-0fc2-4bab-ac64-cfb7328403bb"
        val webhook = WebHook(
                "c7f99d9f-0fc2-4bab-ac64-cfb7328403bb",
                "name_param",
                WebHook.EventType.CREATED,
                "url_dummy")

        val mock_db = mutableMapOf<String, WebHookDocument>(webHookId to WebHookDocument(webhook))

        val mockWebHookStore: WebHookStore = WebHookStoreStub(mock_db)
        val mockWebHookService: WebHookService = WebHookService(mockWebHookStore)
        val webhookQueryResolver: WebHookQueryResolver = WebHookQueryResolver(mockWebHookService)

//        every { mockWebHookStore.findWebHook(webHookId) }.returns(Mono.just(webhook))

        val webhookOutput = webhookQueryResolver.getWebHook(webHookId)

        webhookOutput.`should equal`(WebHookOutput.fromWebHook(webhook))
    }
}