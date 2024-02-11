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

import io.github.breninsul.webfluxlogging.CommonLoggingUtils
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

/**
 * Utility class for logging HTTP communication in Spring Cloud Gateway applications.
 *
 * Provides features to log request and response details including headers, body, timing etc.
 * Logging of each aspect can be controlled individually.
 *
 * @property maxBodySize The maximum size of the body content to log, in bytes.
 * @property logger The Logger to use for logging.
 * @property loggingLevel The logging level at which to log the details.
 * @property logTime Whether to log the time taken to handle the request.
 * @property logHeaders Whether to log the request and response headers.
 * @property logBody Whether to log the request and response body.
 * @property commonUtils Common utilities for logging.
 */
open class SpringCloudGatewayLoggingUtils(
    val maxBodySize: Int,
    protected val logger: Logger,
    protected val loggingLevel: Level,
    protected val logTime: Boolean,
    protected val logHeaders: Boolean,
    protected val logBody: Boolean,
    protected val commonUtils: CommonLoggingUtils = CommonLoggingUtils(),
) {
    /**
     * Method to obtain the query parameters from the request.
     * The query parameters are returned as a single String.
     *
     * @param request The Server HTTP Request from which to obtain the query parameters.
     * @return A string containing the query parameters.
     */
    protected open fun getParams(request: ServerHttpRequest): String {
        val params =
            request
                .queryParams
                .entries
                .map { "${it.key}:${it.value.joinToString(",")}" }.joinToString("&")
        return request.path.toString() + (if (params.isBlank()) params else "?$params")
    }

    /**
     * Method to log a message and optionally an exception.
     *
     * @param logString The message to log.
     * @param t The exception to log along with the message. Default is null, in which case no exception is logged.
     */
    open fun log(
        logString: String,
        t: Throwable? = null,
    ) {
        if (t != null) {
            logger.log(loggingLevel, logString, t)
        } else {
            logger.log(loggingLevel, logString)
        }
    }

    /**
     * Method to obtain the content of multipart request parts.
     * The part names and their content is returned as a single String.
     *
     * @param data The MultiValueMap of parts from which to obtain the content.
     * @return A Mono<String> representing the content of the parts.
     */
    open fun getPartsContent(data: MultiValueMap<String, Part>): Mono<String> {
        return Flux.fromIterable(data.values.flatten().map { getPartContest(it) }).flatMap { it }.collectList()
            .map { it.joinToString(";") }
    }

    /**
     * Helper method to get the content of a single part of a multipart request.
     *
     * @param it The Part from which to obtain the content.
     * @return A Mono<String> representing the content of the part.
     */
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
                return@map "${it.name()}:$l"
            }
        }
    }

    /**
     * Method to convert a size in bytes to a human readable format.
     *
     * @param bytes The size in bytes.
     * @return A string representing the size in a human readable format.
     */
    protected open fun bytesToHumanReadableSize(bytes: Double) =
        when {
            bytes >= 1 shl 30 -> "%.1f GB".format(bytes / (1 shl 30))
            bytes >= 1 shl 20 -> "%.1f MB".format(bytes / (1 shl 20))
            bytes >= 1 shl 10 -> "%.0f kB".format(bytes / (1 shl 10))
            else -> "$bytes bytes"
        }

    /**
     * Method to write the details of a HTTP response to the log.
     *
     * @param request The Server HTTP Request associated with the response.
     * @param response The Server HTTP Response to log the details of.
     * @param data The body content of the response to log. Should be of a size no greater than `maxBodySize`.
     * @param startTime The time at which the request handling started. Used to calculate and log the time taken.
     */
    open fun writeResponse(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        data: DataBuffer?,
        startTime: Long?,
    ) {
        writeResponse(request, response, commonUtils.getContent(data, maxBodySize), startTime)
    }

    /**
     * Overloaded method to write the details of a HTTP response to the log.
     * This method accepts the body content as a string.
     *
     * @param request The Server HTTP Request associated with the response.
     * @param response The Server HTTP Response to log the details of.
     * @param data The body content of the response to log as a string. Should be of a size no greater than `maxBodySize`.
     * @param startTime The time at which the request handling started. Used to calculate and log the time taken.
     */
    protected open fun writeResponse(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        data: String?,
        startTime: Long?,
    ) {
        val timeString =
            if (logTime && startTime != null) ("\n=Took         : ${System.currentTimeMillis() - startTime} ms") else ""
        val headersString =
            if (logHeaders) ("\n=Headers      : ${commonUtils.getHeadersContent(response.headers)}") else ""
        val bodyString =
            if (logBody) ("\n=Body         : ${commonUtils.getBodyContent(data, maxBodySize)}") else ""
        val logString =
            """

            ===========================SERVER Gateway response begin===========================
            =ID           : ${request.id}
            =URI          : ${request.method}  ${response.statusCode?.value()} ${getParams(request)}$timeString$headersString$bodyString
            ===========================SERVER Gateway response end   ==========================
            """.trimIndent()
        log(logString)
    }

    open fun writeRequest(
        request: ServerHttpRequest,
        data: DataBuffer?,
    ) {
        writeRequest(request, commonUtils.getContent(data, maxBodySize))
    }

    open fun writeRequest(
        request: ServerHttpRequest,
        data: String?,
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
            ===========================SERVER Gateway request end   ==========================
            """.trimIndent()

        log(logString)
    }
}
