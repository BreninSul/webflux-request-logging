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

package com.github.breninsul.webfluxlogging.client

import org.slf4j.Logger
import org.slf4j.event.Level
import org.slf4j.spi.LoggingEventBuilder
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

open class WebClientLoggingExchangeFilterFunction(
    protected val loggingUtils:WebClientLoggingUtils,
) : ExchangeFilterFunction {

    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        val startTime = System.currentTimeMillis()
        val loggedRequest =
            WebClientLoggingRequestBodyInserter(
                request,
                request.body(),
                loggingUtils
            ).createLoggedRequest()
        val responseMono = next.exchange(loggedRequest)
            .map { response ->
                var processed:Boolean=false
                response
                    .mutate()
                    .body { bytesFlux ->
                        DataBufferUtils.join(bytesFlux)
                            .publishOn(Schedulers.boundedElastic())
                            .switchIfEmpty{
                                //If no content  (Rs without body) - Flux will not be invoked, this switchIfEmpty just log request with empty body
                                Mono.defer {
                                    if (!processed) {
                                        processed=true
                                        loggingUtils.writeResponse(
                                            request,
                                            response,
                                            null,
                                            startTime
                                        )
                                    }
                                    Mono.empty<DataBuffer>()
                                }
                            }
                            .map {
                                processed=true
                                loggingUtils.writeResponse(
                                    loggedRequest,
                                    response,
                                    it,
                                    startTime
                                )
                                it
                            }
                            .flux()
                    }.build()
            }
        return responseMono
    }

    protected fun <T> Mono<T>.switchIfEmpty(s: () -> Mono<T>): Mono<T> = this.switchIfEmpty(Mono.defer { s() })

}