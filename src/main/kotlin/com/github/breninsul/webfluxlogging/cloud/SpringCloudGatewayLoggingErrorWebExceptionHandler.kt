package com.github.breninsul.webfluxlogging.cloud

import com.github.breninsul.webfluxlogging.cloud.SpringCloudGatewayLoggingFilter.Companion.START_TIME_ATTRIBUTE
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
    protected val maxBodySize: Int,
    protected val addIdHeader: Boolean,
    protected val logTime: Boolean,
    protected val logHeaders: Boolean,
    protected val logBody: Boolean,
    protected val logger: LoggingEventBuilder,
     errorAttributes: ErrorAttributes,
     resources: WebProperties.Resources,
     errorProperties: ErrorProperties,
     applicationContext: ApplicationContext,

    ) : DefaultErrorWebExceptionHandler(errorAttributes, resources, errorProperties, applicationContext) {
    override fun renderErrorResponse(request: ServerRequest): Mono<ServerResponse> {
        val rsMono = super.renderErrorResponse(request)
        return rsMono.map { ResponseLoggingErrorInterceptor(maxBodySize,addIdHeader,logTime,logHeaders,logBody,logger, it) }
    }

    override fun acceptsTextHtml(): RequestPredicate {
        return RequestPredicate { serverRequest: ServerRequest ->
            false
        }
    }

    class ResponseLoggingErrorInterceptor(
        protected val maxBodySize: Int,
        protected val addIdHeader: Boolean,
        protected val logTime: Boolean,
        protected val logHeaders: Boolean,
        protected val logBody: Boolean,
        protected val logger: LoggingEventBuilder,
        protected val delegateRs:ServerResponse
    ) : ServerResponse by delegateRs {
        override fun writeTo(exchange: ServerWebExchange, context: ServerResponse.Context): Mono<Void> {
            val withLog = exchange.mutate().response(
                SpringCloudGatewayLoggingResponseInterceptor(
                    maxBodySize,
                    addIdHeader,
                    logTime,
                    logHeaders,
                    logBody,
                    exchange.response,
                    exchange.request,
                    logger,
                    exchange.attributes[START_TIME_ATTRIBUTE] as Long?
                )
            ).build()
            return delegateRs.writeTo(withLog, context)
        }
    }
}