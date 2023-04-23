package com.github.breninsul.webfluxlogging.client

import org.reactivestreams.Publisher
import org.slf4j.spi.LoggingEventBuilder
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.client.reactive.ClientHttpRequest
import org.springframework.web.reactive.function.client.ClientRequest
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

open class WebClientLoggingClientHttpRequest(
    protected val request: ClientRequest,
    protected val delegate: ClientHttpRequest,
    protected val loggingUtils:WebClientLoggingUtils,
) : ClientHttpRequest by delegate {
    override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
        return delegate.writeWith(
            Mono.from(body)
                .publishOn(Schedulers.boundedElastic())
                .doOnNext {
                    val position=it.readPosition()
                    val data = String(it.asInputStream().readAllBytes())
                    it.readPosition(position)
                    loggingUtils.writeRequest(request, data)
                })
    }

    override fun writeAndFlushWith(body: Publisher<out Publisher<out DataBuffer>>): Mono<Void> {
        return delegate.writeAndFlushWith(body)
    }


}