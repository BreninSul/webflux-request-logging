package com.github.breninsul.webfluxlogging.cloud

import org.slf4j.spi.LoggingEventBuilder
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

open class SpringCloudGatewayLoggingRequestInterceptor(
    protected val delegateRq: ServerHttpRequest,
    protected val loggingUtils: SpringCloudGatewayLoggingUtils,
) : ServerHttpRequestDecorator(delegateRq) {
    override fun getBody(): Flux<DataBuffer> {
        return super
            .getBody()
            .publishOn(Schedulers.boundedElastic())
            .doOnNext { dataBuffer: DataBuffer ->
                try {
                    loggingUtils.writeRequest( delegateRq, dataBuffer)
                } catch (e: Throwable) {
                    loggingUtils.logger.log("Error in request filter", e)
                }
            }
    }


}