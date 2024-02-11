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

import org.reactivestreams.Publisher
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.http.server.reactive.ServerHttpResponseDecorator
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * Class for implementing response interceptor in Spring cloud gateway with logging.
 *
 * @property addIdHeader If true, adds a header with request id to the response.
 * @property delegateRs The original response to which operations will be delegated.
 * @property delegateRq The request to get details from.
 * @property startTime The time when the request was received.
 * @property utils Helper utilities for logging.
 * @property idHeader The name of the header where request id is specified.
 */
open class SpringCloudGatewayLoggingResponseInterceptor(
    protected val addIdHeader: Boolean,
    protected val delegateRs: ServerHttpResponse,
    protected val delegateRq: ServerHttpRequest,
    protected val startTime: Long?,
    protected val utils: SpringCloudGatewayLoggingUtils,
    protected val idHeader: String = "X-Request-Id",
) :
    ServerHttpResponseDecorator(delegateRs) {
    /**
     * Handler method for response's body.
     *
     * @param body The response's body.
     * @return Mono<Void> for chaining the response.
     */
    override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
        if (addIdHeader) {
            delegateRs.headers.add(idHeader, delegateRq.id)
        }
        val buffer = DataBufferUtils.join(body)
        val dataBufferFlux =
            buffer
                .publishOn(Schedulers.boundedElastic())
                .switchIfEmpty(
                    Mono.defer {
                        utils.writeResponse(delegateRq, delegateRs, null as DataBuffer?, startTime)
                        return@defer Mono.empty<DataBuffer>()
                    } as Mono<DataBuffer>,
                )
                .doOnNext { dataBuffer: DataBuffer ->
                    try {
                        utils.writeResponse(delegateRq, delegateRs, dataBuffer, startTime)
                    } catch (e: Throwable) {
                        utils.log("Error in response filter", e)
                    }
                }
        return super.writeWith(dataBufferFlux)
    }
}