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

package io.github.breninsul.webfluxlogging.cloud

import org.springframework.boot.autoconfigure.web.ErrorProperties
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.web.reactive.function.server.RequestPredicate
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * Error handling class with logging functionality.
 *
 * @property addIdHeader Determines whether to add an id header to the response.
 * @property utils The utility object for the logging operations.
 * @property errorAttributes Attributes representing an error.
 * @property resources Resources related to web properties.
 * @property errorProperties Configuration options for error handling.
 * @property applicationContext The application context.
 */
open class SpringCloudGatewayLoggingErrorWebExceptionHandler(
    protected val addIdHeader: Boolean,
    protected val utils: SpringCloudGatewayLoggingUtils,
    errorAttributes: ErrorAttributes,
    resources: WebProperties.Resources,
    errorProperties: ErrorProperties,
    applicationContext: ApplicationContext,
) : DefaultErrorWebExceptionHandler(errorAttributes, resources, errorProperties, applicationContext) {

    /**
     * Handles errors and logs them.
     *
     * @param request The server request.
     * @return The server response.
     */
    override fun renderErrorResponse(request: ServerRequest): Mono<ServerResponse> {
        val rsMono = super.renderErrorResponse(request)
        return rsMono.map { ResponseLoggingErrorInterceptor(addIdHeader, it, utils) }
    }

    /**
     * Determines whether the response should be in HTML format.
     *
     * @return A RequestPredicate indicating the desired response format.
     */
    override fun acceptsTextHtml(): RequestPredicate {
        return RequestPredicate { serverRequest: ServerRequest ->
            false
        }
    }

    /**
     * Intercepts the server response for logging purposes.
     *
     * @property addIdHeader Determines whether to add an id header to the response.
     * @property delegateRs The server response to be logged.
     * @property utils The utility object for the logging operations.
     * @property startTimeAttribute The attribute representing the start time of the request.
     */
    open class ResponseLoggingErrorInterceptor(
        protected val addIdHeader: Boolean,
        protected val delegateRs: ServerResponse,
        protected val utils: SpringCloudGatewayLoggingUtils,
        protected val startTimeAttribute: String = "startTime",
    ) : ServerResponse by delegateRs {

        /**
         * Logs the server response and writes the response to the output.
         *
         * @param exchange The server web exchange.
         * @param context The server response context.
         * @return A Mono indicating the completion of the operation.
         */
        override fun writeTo(
            exchange: ServerWebExchange,
            context: ServerResponse.Context,
        ): Mono<Void> {
            val withLog =
                exchange.mutate().response(
                    SpringCloudGatewayLoggingResponseInterceptor(
                        addIdHeader,
                        exchange.response,
                        exchange.request,
                        exchange.attributes[startTimeAttribute] as Long?,
                        utils,
                    ),
                ).build()
            return delegateRs.writeTo(withLog, context)
        }
    }
}
