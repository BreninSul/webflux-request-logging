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

import org.springframework.http.client.reactive.ClientHttpRequest
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.client.ClientRequest
import reactor.core.publisher.Mono

open class WebClientLoggingRequestBodyInserter(
    protected val request: ClientRequest,
    protected val delegate: BodyInserter<*, in ClientHttpRequest>,
    protected val loggingUtils: WebClientLoggingUtils,
) : BodyInserter<Any, ClientHttpRequest> {
    protected val loggedRequest = ClientRequest.from(request).body(this).build()
    open fun createLoggedRequest(): ClientRequest {
        return loggedRequest
    }

    override fun insert(outputMessage: ClientHttpRequest, context: BodyInserter.Context): Mono<Void> {
        outputMessage.beforeCommit {
            Mono.defer {
                //If no content (Rq without body) - BodyInserter will not be invoked. Have to invoke logging directly
                val bodyExist = outputMessage.headers.contentLength > 0
                if (!bodyExist) {
                    loggingUtils.writeRequest(request, null)
                }
                Mono.empty<Void>()
            }
        }
        return delegate.insert(
            WebClientLoggingClientHttpRequest(
                request,
                outputMessage,
                loggingUtils,
            ), context
        )
    }
}