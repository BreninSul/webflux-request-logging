package com.github.breninsul.webfluxlogging.cloud

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.MediaType
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono


class SpringCloudGatewayLoggingFilter(
    protected val addIdHeader: Boolean,
    protected val utils:SpringCloudGatewayLoggingUtils,
    protected val order: Int = Int.MIN_VALUE,
    protected val idHeader:String = "X-Request-Id",
    protected val startTimeAttribute:String = "startTime",
    ) : GlobalFilter, Ordered {

    override fun getOrder(): Int {
        return order
    }

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        exchange.attributes[startTimeAttribute] = System.currentTimeMillis()
        val withId = if (addIdHeader) exchange.request.mutate().header(idHeader, exchange.request.id).build() else exchange.request
        val contentType = withId.headers.contentType
        val isMultipart = contentType?.includes(MediaType.MULTIPART_FORM_DATA) ?: false
        val requestWithFilter = SpringCloudGatewayLoggingRequestInterceptor( withId,utils)
        val responseWithFilter = SpringCloudGatewayLoggingResponseInterceptor(
            addIdHeader,
            exchange.response,
            withId,
            exchange.attributes[startTimeAttribute] as Long?,
            utils
        )
        val loggingWebExchange = exchange.mutate()
            .request(requestWithFilter)
            .response(responseWithFilter)
            .build()
        if (isMultipart) {
            val log = exchange.multipartData.flatMap { utils.getPartsContent(it) }.doOnNext { data ->
                utils.writeRequest(withId,data)
            }
            return log.and(chain.filter(loggingWebExchange))
        }
        return chain.filter(loggingWebExchange)
    }

}