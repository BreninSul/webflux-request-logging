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

class SpringCloudGatewayLoggingResponseInterceptor(
    protected val maxBodySize: Int,
    protected val addIdHeader: Boolean,
    protected val logTime: Boolean,
    protected val logHeaders: Boolean,
    protected val logBody: Boolean,
    protected val delegateRs: ServerHttpResponse,
    protected val delegateRq: ServerHttpRequest,
    protected val logger: LoggingEventBuilder,
    protected val startTime: Long?,
    ) :
    ServerHttpResponseDecorator(delegateRs) {


    override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
        if (addIdHeader) {
            delegateRs.headers.add(ID_HEADER, delegateRq.id)
        }
        val buffer = Flux.from(body)
        return super.writeWith(buffer
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
                SpringCloudGatewayLoggingUtils.INSTANCE.writeResponse(maxBodySize,logger,logTime,logHeaders,logBody,delegateRq,delegateRs,content,startTime)
            } catch (e: Throwable) {
                logger.log("Error in response filter", e)
            }
        })
    }
    companion object{
         const val ID_HEADER = "X-Request-Id"
    }
}