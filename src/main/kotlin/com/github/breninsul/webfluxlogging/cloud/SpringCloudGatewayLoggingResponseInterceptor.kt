package com.github.breninsul.webfluxlogging.cloud

import org.reactivestreams.Publisher
import org.slf4j.spi.LoggingEventBuilder
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.http.server.reactive.ServerHttpResponseDecorator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

open class SpringCloudGatewayLoggingResponseInterceptor(
    protected val addIdHeader: Boolean,
    protected val delegateRs: ServerHttpResponse,
    protected val delegateRq: ServerHttpRequest,
    protected val startTime: Long?,
    protected val utils: SpringCloudGatewayLoggingUtils,
    protected val idHeader:String = "X-Request-Id"
    ) :
    ServerHttpResponseDecorator(delegateRs) {


    override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
        if (addIdHeader) {
            delegateRs.headers.add(idHeader, delegateRq.id)
        }
        val buffer = Mono.from(body)
        val dataBufferFlux = buffer
            .publishOn(Schedulers.boundedElastic())
            .switchIfEmpty (
               Mono.defer {
                    utils.writeResponse(delegateRq, delegateRs, null as DataBuffer?, startTime)
                    return@defer Mono.empty< DataBuffer>()
                } as Mono<DataBuffer>
            )
            .doOnNext { dataBuffer: DataBuffer ->
                try {
                    utils.writeResponse(delegateRq, delegateRs, dataBuffer, startTime)
                } catch (e: Throwable) {
                    utils.log("Error in response filter", e)
                }
            }
        return super.writeWith(dataBufferFlux)
    }
}