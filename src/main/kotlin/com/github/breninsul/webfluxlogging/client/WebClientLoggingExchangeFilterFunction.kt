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

class WebClientLoggingExchangeFilterFunction(
    protected val maxBodySize: Int,
    protected val loggerRaw: Logger,
    protected val logLevel: Level,
    protected val logTime: Boolean,
    protected val logHeaders: Boolean,
    protected val logBody: Boolean,
) : ExchangeFilterFunction {
    protected val logger: LoggingEventBuilder = loggerRaw.atLevel(logLevel)

    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        val startTime = System.currentTimeMillis()
        //If no content log without body - BodyInserter will not be invoked
        val bodyExist = request.headers().contentLength > 0
        if (!bodyExist) {
            WebClientLogingUtils.INSTANCE.writeRequest(maxBodySize, logger, logHeaders, logBody, request, null)
        }
        val loggedRequest = if (bodyExist)
            WebClientLoggingRequestBodyInserter(
                maxBodySize,
                logger,
                logHeaders,
                logBody,
                request,
                request.body()
            ).createLoggedRequest()
        else request
        val responseMono = next.exchange(loggedRequest)
            .map { response ->
                response
                    .mutate()
                    .body { bytesFlux ->
                        DataBufferUtils.join(bytesFlux)
                            .switchIfEmpty{
                                //If no content log without body - Flux will not be invoked
                                WebClientLogingUtils.INSTANCE.writeResponse(
                                    maxBodySize,
                                    logger,
                                    logTime,
                                    logHeaders,
                                    logBody,
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
                                WebClientLogingUtils.INSTANCE.writeResponse(
                                    maxBodySize,
                                    logger,
                                    logTime,
                                    logHeaders,
                                    logBody,
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