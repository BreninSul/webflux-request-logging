package com.github.breninsul.webfluxlogging.cloud

import org.slf4j.spi.LoggingEventBuilder
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

class SpringCloudGatewayLoggingRequestInterceptor(
    protected val maxBodySize: Int,
    protected val logHeaders: Boolean,
    protected val logBody: Boolean,
    protected val delegateRq: ServerHttpRequest,
    protected val logger: LoggingEventBuilder,
) : ServerHttpRequestDecorator(delegateRq) {
    override fun getBody(): Flux<DataBuffer> {
        return super
            .getBody()
            .publishOn(Schedulers.boundedElastic())
            .doOnNext { dataBuffer: DataBuffer ->
                try {
                    val contentLength = dataBuffer.readableByteCount() - dataBuffer.readPosition();
                    val content = if (contentLength > maxBodySize) "<TOO BIG $contentLength bytes>" else {
                        val position=dataBuffer.readPosition()
                        val body=String(dataBuffer.asInputStream().readAllBytes())
                        dataBuffer.readPosition(position)
                        body
                    }
                    SpringCloudGatewayLoggingUtils.INSTANCE.writeRequest(maxBodySize,logger,logHeaders,logBody, delegateRq, content)
                } catch (e: Throwable) {
                    logger.log("Error in request filter", e)
                }
            }
    }


}