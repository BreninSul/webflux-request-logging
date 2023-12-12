/*
 * MIT License
 *
 * Copyright (c) 2023 BreninSul
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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