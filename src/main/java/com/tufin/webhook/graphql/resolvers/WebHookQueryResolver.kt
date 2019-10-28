package com.tufin.webhook.graphql.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.tufin.webhook.graphql.output.WebHookOutput
import com.tufin.webhook.graphql.output.WebHookOutput.Companion.fromWebHook
import com.tufin.webhook.service.WebHookService
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val LOGGER = KotlinLogging.logger {}

@Component
internal class WebHookQueryResolver(private val webHookService : WebHookService) : GraphQLQueryResolver {

    fun getWebHook(id: String) : WebHookOutput{
        var success = true
        return webHookService
                .getWebHook(id)
                .doOnError { success = false }
                .map { WebHookOutput(it.uid, it.name, it.url) }
                .block()
    }

    fun getAllWebHooks():List<WebHookOutput>{
        var success = true
        return webHookService
                .getAllWebHooks()
                .doOnError{ success = false}
                .map { fromWebHook(it)}
                .collectList()
                .block()!!

    }

}

