package com.tufin.webhook.graphql.resolvers

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import com.tufin.webhook.graphql.input.WebHookInput
import com.tufin.webhook.graphql.output.WebHookOutput
import com.tufin.webhook.model.WebHook
import com.tufin.webhook.service.WebHookService
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

private val LOGGER = KotlinLogging.logger {}

@Component
class WebHookMutationResolver(private val webHookService : WebHookService) : GraphQLMutationResolver {

    fun registerWebHook(webHookInput: WebHookInput): WebHookOutput {
        var success = true
        val webHook = webHookInput.toWebHook(UUID.randomUUID().toString())
        webHookService
                .registerWebHook(webHook)
                .doOnError { success = false }
                .block()

        return if (success)
            return WebHookOutput(webHook.uid, webHook.name, webHook.url)
        else
            WebHookOutput(name = webHook.name, url = webHook.url)
    }

    fun updateWebHook(uid: String, webHookInput: WebHookInput): Boolean {
        var success = true
        webHookService.updateWebHook(uid, webHookInput.toWebHook(uid))
                .doOnError { success = false}
                .block()
        return success
    }

    fun deleteWebhook(uid: String): Boolean {
        var success = true
        webHookService.deleteWebhook(uid).doOnError { success = false}.block()
        return success
    }
}
