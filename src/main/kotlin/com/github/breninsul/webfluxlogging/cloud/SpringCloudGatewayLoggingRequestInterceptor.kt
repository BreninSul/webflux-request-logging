package com.github.breninsul.webfluxlogging.cloud

import org.slf4j.spi.LoggingEventBuilder
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpMethod
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

open class SpringCloudGatewayLoggingRequestInterceptor(
    protected val delegateRq: ServerHttpRequest,
    protected val loggingUtils: SpringCloudGatewayLoggingUtils,
) : ServerHttpRequestDecorator(delegateRq) {
    override fun getBody(): Flux<DataBuffer> {
        val flux = DataBufferUtils.join(super
            .getBody())
            .publishOn(Schedulers.boundedElastic())
            .switchIfEmpty (
                Mono.defer {
                    loggingUtils.writeRequest(delegateRq, null as DataBuffer?)
                    Mono.empty<DataBuffer>()
                }
            )
            .doOnNext { dataBuffer: DataBuffer ->
                try {
                    loggingUtils.writeRequest(delegateRq, dataBuffer)
                } catch (e: Throwable) {
                    loggingUtils.log("Error in request filter", e)
                }
            }.flux()
        return if (delegateRq.method == HttpMethod.GET) {
            val cached = flux.cache()
            cached.subscribeOn(Schedulers.boundedElastic()).subscribe()
            cached
        } else {
            flux
        }
    }


}