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

package io.github.breninsul.webfluxlogging.client

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * WebClientLoggingExchangeFilterFunction is an Exchange Filter Function that
 * enables logging of WebClient requests and responses.
 *
 * @property loggingUtils WebClientLoggingUtils which provides utility functions for logging
 */
open class WebClientLoggingExchangeFilterFunction(
    protected val loggingUtils: WebClientLoggingUtils,
) : ExchangeFilterFunction {

    /**
     * The filter function for WebClient requests and responses. It logs the
     * request message and details and the response status, headers and body.
     *
     * @property request the ClientRequest object which comprises of the HTTP method, URI, headers, and body of the
     * request that is getting logged.
     * @property next the next function in the chain to be executed with the request and response.
     * @return a Mono<ClientResponse> which is the HTTP response wrapped in a Mono wrapper.
     */
    override fun filter(
        request: ClientRequest,
        next: ExchangeFunction,
    ): Mono<ClientResponse> {
        val startTime = System.currentTimeMillis()
        val loggedRequest =
            WebClientLoggingRequestBodyInserter(
                request,
                request.body(),
                loggingUtils,
            ).createLoggedRequest()
        val responseMono =
            next.exchange(loggedRequest)
                .map { response ->
                    var processed: Boolean = false
                    response
                        .mutate()
                        .body { bytesFlux ->
                            DataBufferUtils.join(bytesFlux)
                                .publishOn(Schedulers.boundedElastic())
                                .switchIfEmpty {
                                    // If no content  (Rs without body) - Flux will not be invoked, this switchIfEmpty just log request with empty body
                                    Mono.defer {
                                        if (!processed) {
                                            processed = true
                                            loggingUtils.writeResponse(
                                                request,
                                                response,
                                                null,
                                                startTime,
                                            )
                                        }
                                        Mono.empty<DataBuffer>()
                                    }
                                }
                                .map {
                                    processed = true
                                    loggingUtils.writeResponse(
                                        loggedRequest,
                                        response,
                                        it,
                                        startTime,
                                    )
                                    it
                                }
                                .flux()
                        }.build()
                }
        return responseMono
    }

    /**
     * An extension function for Mono. It checks the Mono if it emits an item,
     * if not it switches to another Mono provided by the supplier function.
     *
     * @param s a supplier function that returns a Mono<T>.
     * @return a Mono<T> which either the original Mono or a new Mono provides by the supplier function.
     */
    protected open fun <T> Mono<T>.switchIfEmpty(s: () -> Mono<T>): Mono<T> = this.switchIfEmpty(Mono.defer { s() })
}
