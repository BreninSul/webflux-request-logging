package com.github.breninsul.webfluxlogging.cloud

import org.slf4j.Logger
import org.slf4j.spi.LoggingEventBuilder
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.MediaType
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono


class SpringCloudGatewayLoggingFilter(
    protected val maxBodySize: Int,
    protected val loggerRaw:Logger,
    protected val logLevel: org.slf4j.event.Level,
    protected val addIdHeader: Boolean,
    protected val logTime: Boolean,
    protected val logHeaders: Boolean,
    protected val logBody: Boolean,
    protected val order: Int = Int.MIN_VALUE,
) : GlobalFilter, Ordered {
    protected val logger: LoggingEventBuilder = loggerRaw.atLevel(logLevel)

    override fun getOrder(): Int {
        return order
    }

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        exchange.attributes[START_TIME_ATTRIBUTE] = System.currentTimeMillis()
        val withId = if (addIdHeader) exchange.request.mutate().header(ID_HEADER, exchange.request.id).build() else exchange.request
        val contentType = withId.headers.contentType
        val isMultipart = contentType?.includes(MediaType.MULTIPART_FORM_DATA) ?: false
        val requestWithFilter = SpringCloudGatewayLoggingRequestInterceptor(maxBodySize,logHeaders,logBody, withId,logger)
        val responseWithFilter = SpringCloudGatewayLoggingResponseInterceptor(
            maxBodySize,
            addIdHeader,
            logTime,
            logHeaders,
            logBody,
            exchange.response,
            withId,
            logger,
            exchange.attributes[START_TIME_ATTRIBUTE] as Long?
        )
        val loggingWebExchange = exchange.mutate()
            .request(requestWithFilter)
            .response(responseWithFilter)
            .build()
        if (isMultipart) {
            val log = exchange.multipartData.flatMap { SpringCloudGatewayLoggingUtils.INSTANCE.getPartsContent(it) }.doOnNext { data ->
                SpringCloudGatewayLoggingUtils.INSTANCE.writeRequest(maxBodySize,logger,logHeaders,logBody,withId,data)
            }
            return log.and(chain.filter(loggingWebExchange))
        }
        return chain.filter(loggingWebExchange)
    }


    companion object {
        const val ID_HEADER = "X-Request-Id"
        const val START_TIME_ATTRIBUTE = "startTime"
    }

}