package com.github.breninsul.webfluxlogging.client

import com.github.breninsul.webfluxlogging.CommonLoggingUtils
import org.slf4j.spi.LoggingEventBuilder
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse

open class WebClientLoggingUtils (
    protected val maxBodySize: Int,
    protected val logger: LoggingEventBuilder,
    protected val logTime:Boolean,
    protected val logHeaders: Boolean,
    protected val logBody: Boolean,
    protected val commonUtils:CommonLoggingUtils = CommonLoggingUtils()) {
    @Bean
    @ConditionalOnMissingBean
    fun getCommonLoggingUtils(
    ): CommonLoggingUtils {
        return CommonLoggingUtils()
    }
    public fun writeRequest(
        request: ClientRequest,
        data: DataBuffer?
    ) {
        writeRequest(request,commonUtils.getContent(data,maxBodySize))
    }
    protected fun writeRequest(
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
        logger.log(logString)
    }
    public fun writeResponse(
        request: ClientRequest,
        response: ClientResponse,
        data: DataBuffer?,
        startTime: Long
    ) {
        writeResponse(request,response,commonUtils.getContent(data,maxBodySize),startTime)
    }
    protected fun writeResponse(
        request: ClientRequest,
        response: ClientResponse,
        data: String?,
        startTime: Long
    ) {
        val timeString= if (logTime) ("\n=Took         : ${System.currentTimeMillis() - startTime} ms") else ""
        val headersString = if (logHeaders) ("\n=Headers      : ${commonUtils.getHeadersContent(response.headers().asHttpHeaders())}") else ""
        val bodyString = if (logBody) ("\n=Body         : ${commonUtils.getBodyContent(data, maxBodySize)}") else ""
        val logString = """

===========================WebClient response begin===========================
=ID           : ${request.logPrefix()}
=URI          : ${response.statusCode()} ${request.method()} ${request.url()}$timeString$headersString$bodyString
===========================WebClient response end   ==========================""".trimIndent()
        logger.log( logString)
    }


}