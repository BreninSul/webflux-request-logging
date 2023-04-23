package com.github.breninsul.webfluxlogging.client

import org.springframework.http.client.reactive.ClientHttpRequest
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.client.ClientRequest
import reactor.core.publisher.Mono

class WebClientLoggingRequestBodyInserter(
    protected val request: ClientRequest,
    protected val delegate: BodyInserter<*, in ClientHttpRequest>,
    protected val loggingUtils:WebClientLoggingUtils,
    ) : BodyInserter<Any, ClientHttpRequest> {
    protected val loggedRequest = ClientRequest.from(request).body(this).build()
    fun createLoggedRequest(): ClientRequest {
        return loggedRequest
    }

    override fun insert(outputMessage: ClientHttpRequest, context: BodyInserter.Context): Mono<Void> {
        return delegate.insert(
            WebClientLoggingClientHttpRequest(
                request,
                outputMessage,
                loggingUtils,
            ), context
        )
    }
}