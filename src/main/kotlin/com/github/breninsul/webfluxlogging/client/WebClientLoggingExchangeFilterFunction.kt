package com.github.breninsul.webfluxlogging.client

import org.slf4j.Logger
import org.slf4j.event.Level
import org.slf4j.spi.LoggingEventBuilder
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

open class WebClientLoggingExchangeFilterFunction(
    protected val loggingUtils:WebClientLoggingUtils,
) : ExchangeFilterFunction {

    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        val startTime = System.currentTimeMillis()
        //If no content (Rq without body) - BodyInserter will not be invoked. Have to invoke logging directly
        val bodyExist = request.headers().contentLength > 0
        if (!bodyExist) {
            loggingUtils.writeRequest( request, null)
        }
        val loggedRequest = if (bodyExist)
            WebClientLoggingRequestBodyInserter(
                request,
                request.body(),
                loggingUtils
            ).createLoggedRequest()
        else request
        val responseMono = next.exchange(loggedRequest)
            .map { response ->
                response
                    .mutate()
                    .body { bytesFlux ->
                        DataBufferUtils.join(bytesFlux)
                            .switchIfEmpty{
                                //If no content  (Rs without body) - Flux will not be invoked, this switchIfEmpty just log request with empty body
                                loggingUtils.writeResponse(
                                    request,
                                    response,
                                    null,
                                    startTime
                                )
                                return@switchIfEmpty Mono.empty()
                            }
                            .publishOn(Schedulers.boundedElastic())
                            .map {
                                val position = it.readPosition()
                                loggingUtils.writeResponse(
                                    loggedRequest,
                                    response,
                                    String(it.asInputStream().readAllBytes()),
                                    startTime
                                )
                                it.readPosition(position)
                                it
                            }
                            .flux()
                    }.build()
            }
        return responseMono
    }

    protected fun <T> Mono<T>.switchIfEmpty(s: () -> Mono<T>): Mono<T> = this.switchIfEmpty(Mono.defer { s() })

}