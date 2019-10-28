package com.tufin.webhook.persistence

import com.tufin.webhook.model.WebHook
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface WebHookStore {

	fun countWebHooks(): Mono<Long>

	fun findWebHook(uid: String): Mono<WebHook>

	fun findAllWebHooks(): Flux<WebHook>

	fun insertWebHook(webHook: WebHook): Mono<Boolean>

	fun updateWebHook(uid: String, webHook: WebHook): Mono<Boolean>

	fun deleteWebHook(uid: String): Mono<Boolean>
}