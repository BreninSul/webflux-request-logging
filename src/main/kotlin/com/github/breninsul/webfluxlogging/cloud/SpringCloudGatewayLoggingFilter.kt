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

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.MediaType
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono


class SpringCloudGatewayLoggingFilter(
    protected val addIdHeader: Boolean,
    protected val utils:SpringCloudGatewayLoggingUtils,
    protected val orderValue: Int = Int.MIN_VALUE,
    protected val idHeader:String = "X-Request-Id",
    protected val startTimeAttribute:String = "startTime",
    ) : GlobalFilter, Ordered {

    override fun getOrder(): Int {
        return orderValue
    }

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        exchange.attributes[startTimeAttribute] = System.currentTimeMillis()
        val withId = if (addIdHeader) exchange.request.mutate().header(idHeader, exchange.request.id).build() else exchange.request
        val contentType = withId.headers.contentType
        val isMultipart = contentType?.includes(MediaType.MULTIPART_FORM_DATA) ?: false
        val requestWithFilter = SpringCloudGatewayLoggingRequestInterceptor( withId,utils)
        val responseWithFilter = SpringCloudGatewayLoggingResponseInterceptor(
            addIdHeader,
            exchange.response,
            withId,
            exchange.attributes[startTimeAttribute] as Long?,
            utils
        )
        val loggingWebExchange = exchange.mutate()
            .request(requestWithFilter)
            .response(responseWithFilter)
            .build()
        if (isMultipart) {
            val log = exchange.multipartData.flatMap { utils.getPartsContent(it) }.doOnNext { data ->
                utils.writeRequest(withId,data)
            }
            return log.and(chain.filter(loggingWebExchange))
        }
        return chain.filter(loggingWebExchange)
    }

}