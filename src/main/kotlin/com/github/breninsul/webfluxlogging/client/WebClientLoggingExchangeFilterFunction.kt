package com.github.breninsul.webfluxlogging.client

import org.slf4j.Logger
import org.slf4j.event.Level
import org.slf4j.spi.LoggingEventBuilder
import org.springframework.core.io.buffer.DataBuffer
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
        val loggedRequest =
            WebClientLoggingRequestBodyInserter(
                request,
                request.body(),
                loggingUtils
            ).createLoggedRequest()
        val responseMono = next.exchange(loggedRequest)
            .map { response ->
                var processed:Boolean=false
                response
                    .mutate()
                    .body { bytesFlux ->
                        DataBufferUtils.join(bytesFlux)
                            .publishOn(Schedulers.boundedElastic())
                            .switchIfEmpty{
                                //If no content  (Rs without body) - Flux will not be invoked, this switchIfEmpty just log request with empty body
                                Mono.defer {
                                    if (!processed) {
                                        processed=true
                                        loggingUtils.writeResponse(
                                            request,
                                            response,
                                            null,
                                            startTime
                                        )
                                    }
                                    Mono.empty<DataBuffer>()
                                }
                            }
                            .map {
                                processed=true
                                loggingUtils.writeResponse(
                                    loggedRequest,
                                    response,
                                    it,
                                    startTime
                                )
                                it
                            }
                            .flux()
                    }.build()
            }
        return responseMono
    }

    protected fun <T> Mono<T>.switchIfEmpty(s: () -> Mono<T>): Mono<T> = this.switchIfEmpty(Mono.defer { s() })

}