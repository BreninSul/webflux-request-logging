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
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpMethod
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

open class SpringCloudGatewayLoggingRequestInterceptor(
    protected val delegateRq: ServerHttpRequest,
    protected val loggingUtils: SpringCloudGatewayLoggingUtils,
) : ServerHttpRequestDecorator(delegateRq) {
    override fun getBody(): Flux<DataBuffer> {
        val flux = DataBufferUtils.join(super
            .getBody())
            .publishOn(Schedulers.boundedElastic())
            .switchIfEmpty (
                Mono.defer {
                    loggingUtils.writeRequest(delegateRq, null as DataBuffer?)
                    Mono.empty<DataBuffer>()
                }
            )
            .doOnNext { dataBuffer: DataBuffer ->
                try {
                    loggingUtils.writeRequest(delegateRq, dataBuffer)
                } catch (e: Throwable) {
                    loggingUtils.log("Error in request filter", e)
                }
            }.flux()
        return if (delegateRq.method == HttpMethod.GET) {
            val cached = flux.cache()
            cached.subscribeOn(Schedulers.boundedElastic()).subscribe()
            cached
        } else {
            flux
        }
    }


}