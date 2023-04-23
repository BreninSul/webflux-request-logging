package com.github.breninsul.webfluxlogging.client

import com.github.breninsul.webfluxlogging.CommonLoggingUtils
import org.slf4j.spi.LoggingEventBuilder
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse

class WebClientLogingUtils protected constructor() {

    public fun writeRequest(
        maxBodySize: Int,
        logger: LoggingEventBuilder,
        logHeaders: Boolean,
        logBody: Boolean,
        request: ClientRequest,
        data: String?,
    ) {

        val headersString = if (logHeaders) ("\n=Headers      : ${CommonLoggingUtils.INSTANCE.getHeadersContent(request.headers())}") else ""
        val bodyString = if (logBody) ("\n=Body         : ${CommonLoggingUtils.INSTANCE.getBodyContent(data, maxBodySize)}") else ""
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
        maxBodySize: Int,
        logger: LoggingEventBuilder,
        logTime:Boolean,
        logHeaders: Boolean,
        logBody: Boolean,
        request: ClientRequest,
        response: ClientResponse,
        data: String?,
        startTime: Long
    ) {
        val timeString= if (logTime) ("\n=Took         : ${System.currentTimeMillis() - startTime} ms") else ""
        val headersString = if (logHeaders) ("\n=Headers      : ${CommonLoggingUtils.INSTANCE.getHeadersContent(response.headers().asHttpHeaders())}") else ""
        val bodyString = if (logBody) ("\n=Body         : ${CommonLoggingUtils.INSTANCE.getBodyContent(data, maxBodySize)}") else ""
        val logString = """

===========================WebClient response begin===========================
=ID           : ${request.logPrefix()}
=URI          : ${response.statusCode()} ${request.method()} ${request.url()}$timeString$headersString$bodyString
===========================WebClient response end   ==========================""".trimIndent()
        logger.log( logString)
    }

    companion object {
        @JvmStatic
        val INSTANCE = WebClientLogingUtils()
    }
}