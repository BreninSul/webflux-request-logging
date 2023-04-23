package com.github.breninsul.webfluxlogging.client

import org.slf4j.spi.LoggingEventBuilder
import org.springframework.http.client.reactive.ClientHttpRequest
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.client.ClientRequest
import reactor.core.publisher.Mono

class WebClientLoggingRequestBodyInserter(
    protected val maxBodySize: Int,
    protected val logger: LoggingEventBuilder,
    protected val logHeaders: Boolean,
    protected val logBody: Boolean,
    protected val request: ClientRequest,
    protected val delegate: BodyInserter<*, in ClientHttpRequest>,
) : BodyInserter<Any, ClientHttpRequest> {
    protected val loggedRequest = ClientRequest.from(request).body(this).build()
    fun createLoggedRequest(): ClientRequest {
        return loggedRequest
    }

    override fun insert(outputMessage: ClientHttpRequest, context: BodyInserter.Context): Mono<Void> {
        return delegate.insert(
            WebClientLoggingClientHttpRequest(
                maxBodySize,
                request,
                outputMessage,
                logHeaders,
                logBody,
                logger,
            ), context
        )
    }
}