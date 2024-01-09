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

import com.github.breninsul.webfluxlogging.CommonLoggingUtils
import org.slf4j.spi.LoggingEventBuilder
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.Part
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.util.MultiValueMap
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.logging.Level
import java.util.logging.Logger

open class SpringCloudGatewayLoggingUtils  (
    val maxBodySize: Int,
    protected val logger: Logger,
    protected val loggingLevel:Level,
    protected val logTime: Boolean,
    protected val logHeaders: Boolean,
    protected val logBody: Boolean,
    protected val commonUtils:CommonLoggingUtils = CommonLoggingUtils()) {
    protected open fun getParams(request: ServerHttpRequest): String {
        val params = request
            .queryParams
            .entries
            .map { "${it.key}:${it.value.joinToString(",")}" }.joinToString("&")
        return request.path.toString() + (if (params.isBlank()) params else "?$params")
    }
    open fun log(logString:String,t:Throwable?=null){
        if (t!=null) {
            logger.log(loggingLevel, logString, t)
        } else{
            logger.log(loggingLevel, logString)

        }
    }
    open fun getPartsContent(data: MultiValueMap<String, Part>): Mono<String> {
        return Flux.fromIterable(data.values.flatten().map { getPartContest(it) }).flatMap { it }.collectList()
            .map { it.joinToString(";") }
    }

    protected open fun getPartContest(it: Part): Mono<String> {
        if (it is FilePart) {
            val contentLength =
                it.content().collectList().map { list -> list.sumOf { buffer -> buffer.readableByteCount() } }
            return contentLength.map { l ->
                return@map "${it.name()}:<FILE_BODY ${bytesToHumanReadableSize(l.toDouble())}>"
            }
        } else {
            val content =
                it.content().collectList()
                    .map { list -> list.map { buffer -> String(buffer.asInputStream().readAllBytes()) }.joinToString(";") }
            return content.map { l ->
                return@map "${it.name()}:${l}"
            }
        }
    }

    protected open fun bytesToHumanReadableSize(bytes: Double) = when {
        bytes >= 1 shl 30 -> "%.1f GB".format(bytes / (1 shl 30))
        bytes >= 1 shl 20 -> "%.1f MB".format(bytes / (1 shl 20))
        bytes >= 1 shl 10 -> "%.0f kB".format(bytes / (1 shl 10))
        else -> "$bytes bytes"
    }
    open fun writeResponse(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        data: DataBuffer?,
        startTime: Long?
    ) {
        writeResponse(request,response,commonUtils.getContent(data,maxBodySize),startTime)
    }
    protected open fun writeResponse(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        data: String?,
        startTime: Long?
    ) {
        val timeString =
            if (logTime && startTime != null) ("\n=Took         : ${System.currentTimeMillis() - startTime} ms") else ""
        val headersString =
            if (logHeaders) ("\n=Headers      : ${commonUtils.getHeadersContent(response.headers)}") else ""
        val bodyString =
            if (logBody) ("\n=Body         : ${commonUtils.getBodyContent(data, maxBodySize)}") else ""
        val logString = """

===========================SERVER Gateway response begin===========================
=ID           : ${request.id}
=URI          : ${request.method}  ${response.statusCode?.value()} ${getParams(request)}$timeString$headersString$bodyString
===========================SERVER Gateway response end   ==========================""".trimIndent();
        log(logString)
    }

    open fun writeRequest(
        request: ServerHttpRequest,
        data: DataBuffer?
    ) {
        writeRequest(request,commonUtils.getContent(data,maxBodySize))
    }
    public open fun writeRequest(
        request: ServerHttpRequest,
        data: String?
    ) {
        val headersString =
            if (logHeaders) ("\n=Headers      : ${commonUtils.getHeadersContent(request.headers)}") else ""
        val bodyString =
            if (logBody) ("\n=Body         : ${commonUtils.getBodyContent(data, maxBodySize)}") else ""
        val logString =
            """
===========================SERVER Gateway request begin===========================
=ID           : ${request.id}
=URI          : ${request.method} ${getParams(request)}$headersString$bodyString
===========================SERVER Gateway request end   ==========================""".trimIndent()

        log(logString)
    }


}