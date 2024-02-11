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

import org.reactivestreams.Publisher
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.client.reactive.ClientHttpRequest
import org.springframework.web.reactive.function.client.ClientRequest
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * WebClientLoggingClientHttpRequest is a utility class that wraps a delegate request
 *
 * @property[request] the original client request
 * @property[delegate] the delegated client http request which is to be logged
 * @property[loggingUtils] utility for logging the requests
 */
open class WebClientLoggingClientHttpRequest(
    protected val request: ClientRequest,
    protected val delegate: ClientHttpRequest,
    protected val loggingUtils: WebClientLoggingUtils,
) : ClientHttpRequest by delegate {
    /**
     * writeWith writes the request with a specified body and request
     *
     * @param[body] the body of the request to be logged
     * @return a Mono<Void> after the operation is complete
     */
    override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
        return delegate.writeWith(
            Mono.from(body)
                .publishOn(Schedulers.boundedElastic())
                .doOnNext {
                    loggingUtils.writeRequest(request, it)
                },
        )
    }

    /**
     * writeAndFlushWith writes and flushes the request with a specified body and request
     *
     * @param[body] the body of the request to be logged
     * @return a Mono<Void> after the operation is complete
     */
    override fun writeAndFlushWith(body: Publisher<out Publisher<out DataBuffer>>): Mono<Void> {
        return delegate.writeAndFlushWith(body)
    }
}