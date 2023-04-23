package com.github.breninsul.webfluxlogging.cloud

import org.slf4j.spi.LoggingEventBuilder
import org.springframework.boot.autoconfigure.web.ErrorProperties
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.web.reactive.function.server.*
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono


class SpringCloudGatewayLoggingErrorWebExceptionHandler(
    protected val addIdHeader: Boolean,
    protected val utils:SpringCloudGatewayLoggingUtils,
    errorAttributes: ErrorAttributes,
     resources: WebProperties.Resources,
     errorProperties: ErrorProperties,
     applicationContext: ApplicationContext,
    ) : DefaultErrorWebExceptionHandler(errorAttributes, resources, errorProperties, applicationContext) {
    override fun renderErrorResponse(request: ServerRequest): Mono<ServerResponse> {
        val rsMono = super.renderErrorResponse(request)
        return rsMono.map { ResponseLoggingErrorInterceptor(addIdHeader, it,utils) }
    }

    override fun acceptsTextHtml(): RequestPredicate {
        return RequestPredicate { serverRequest: ServerRequest ->
            false
        }
    }

    class ResponseLoggingErrorInterceptor(
        protected val addIdHeader: Boolean,
        protected val delegateRs:ServerResponse,
        protected val utils:SpringCloudGatewayLoggingUtils,
        protected val startTimeAttribute:String = "startTime",
        ) : ServerResponse by delegateRs {
        override fun writeTo(exchange: ServerWebExchange, context: ServerResponse.Context): Mono<Void> {
            val withLog = exchange.mutate().response(
                SpringCloudGatewayLoggingResponseInterceptor(
                    addIdHeader,
                    exchange.response,
                    exchange.request,
                    exchange.attributes[startTimeAttribute] as Long?,
                    utils
                )
            ).build()
            return delegateRs.writeTo(withLog, context)
        }
    }
}