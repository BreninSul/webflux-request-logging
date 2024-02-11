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

import io.github.breninsul.webfluxlogging.CommonLoggingUtils
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Utility class providing logging functionalities for WebClient-based communication.
 *
 * @property maxBodySize The maximum body size to log
 * @property logger The logger to use
 * @property loggingLevel The logging level to use
 * @property logTime Flag indicating if request/response durations should be logged.
 * @property logHeaders Flag indicating if request/response headers should be logged.
 * @property logBody Flag indicating if request/response bodies should be logged.
 * @property commonUtils An instance of [CommonLoggingUtils] for utility operations
 */
open class WebClientLoggingUtils(
    protected val maxBodySize: Int,
    protected val logger: Logger,
    protected val loggingLevel: Level,
    protected val logTime: Boolean,
    protected val logHeaders: Boolean,
    protected val logBody: Boolean,
    protected val commonUtils: CommonLoggingUtils = CommonLoggingUtils(),
) {
    /**
     * Provides an instance of [CommonLoggingUtils].
     * If there's no bean of [CommonLoggingUtils] defined, a new instance will be created.
     *
     * @return an instance of [CommonLoggingUtils]
     */
    @Bean
    @ConditionalOnMissingBean
    fun getCommonLoggingUtils(): CommonLoggingUtils {
        return CommonLoggingUtils()
    }

    /**
     * Logs a [ClientRequest] and its associated data
     *
     * @param request The request to log
     * @param data The associated data to log
     */
    open fun writeRequest(
        request: ClientRequest,
        data: DataBuffer?,
    ) {
        writeRequest(request, commonUtils.getContent(data, maxBodySize))
    }

    /**
     * Logs a [ClientRequest] and its associated data
     *
     * @param request The request to log
     * @param data The associated data, as a string, to log
     */
    protected open fun writeRequest(
        request: ClientRequest,
        data: String?,
    ) {
        val headersString = if (logHeaders) ("\n=Headers      : ${commonUtils.getHeadersContent(request.headers())}") else ""
        val bodyString = if (logBody) ("\n=Body         : ${commonUtils.getBodyContent(data, maxBodySize)}") else ""
        val logString =
            """
===========================WebClient request begin===========================
=ID           : ${request.logPrefix()}
=URI          : ${request.method()} ${request.url()}$headersString$bodyString
===========================WebClient request end   ==========================
            """.trimMargin()
        logger.log(loggingLevel, logString)
    }

    /**
     * Logs a [ClientRequest]'s response and its associated data
     *
     * @param request The original request
     * @param response The response to log
     * @param data The associated data to log
     * @param startTime The start time of the request, for logging duration
     */
    open fun writeResponse(
        request: ClientRequest,
        response: ClientResponse,
        data: DataBuffer?,
        startTime: Long,
    ) {
        writeResponse(request, response, commonUtils.getContent(data, maxBodySize), startTime)
    }

    /**
     * Logs a [ClientRequest]'s response and its associated data
     *
     * @param request The original request
     * @param response The response to log
     * @param data The associated data, as a string, to log
     * @param startTime The start time of the request, for logging duration
     */
    protected open fun writeResponse(
        request: ClientRequest,
        response: ClientResponse,
        data: String?,
        startTime: Long,
    ) {
        val timeString = if (logTime) ("\n=Took         : ${System.currentTimeMillis() - startTime} ms") else ""
        val headersString = if (logHeaders) ("\n=Headers      : ${commonUtils.getHeadersContent(response.headers().asHttpHeaders())}") else ""
        val bodyString = if (logBody) ("\n=Body         : ${commonUtils.getBodyContent(data, maxBodySize)}") else ""
        val logString =
            """

            ===========================WebClient response begin===========================
            =ID           : ${request.logPrefix()}
            =URI          : ${response.statusCode()} ${request.method()} ${request.url()}$timeString$headersString$bodyString
            ===========================WebClient response end   ==========================
            """.trimIndent()
        logger.log(loggingLevel, logString)
    }
}
