package com.github.breninsul.webfluxlogging.cloud

import com.github.breninsul.webfluxlogging.CommonLoggingUtils
import org.slf4j.spi.LoggingEventBuilder
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.Part
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.util.MultiValueMap
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class SpringCloudGatewayLoggingUtils protected constructor() {
    protected fun getParams(request: ServerHttpRequest): String {
        val params = request
            .queryParams
            .entries
            .map { "${it.key}:${it.value.joinToString(",")}" }.joinToString("&")
        return request.path.toString() + (if (params.isBlank()) params else "?$params")
    }

    public fun getPartsContent(data: MultiValueMap<String, Part>): Mono<String> {
        return Flux.fromIterable(data.values.flatten().map { getPartContest(it) }).flatMap { it }.collectList()
            .map { it.joinToString(";") }
    }

    protected fun getPartContest(it: Part): Mono<String> {
        if (it is FilePart) {
            val contentLength =
                it.content().collectList().map { list -> list.sumOf { buffer -> buffer.readableByteCount() } }
            return contentLength.map { l ->
                return@map "${it.name()}:<FILE_BODY ${bytesToHumanReadableSize(l.toDouble())}>"
            }
        } else {
            val content =
                it.content().collectList()
                    .map { list -> list.map { buffer -> buffer.asInputStream().readAllBytes() }.joinToString(";") }
            return content.map { l ->
                return@map "${it.name()}:${l}"
            }
        }
    }

    protected fun bytesToHumanReadableSize(bytes: Double) = when {
        bytes >= 1 shl 30 -> "%.1f GB".format(bytes / (1 shl 30))
        bytes >= 1 shl 20 -> "%.1f MB".format(bytes / (1 shl 20))
        bytes >= 1 shl 10 -> "%.0f kB".format(bytes / (1 shl 10))
        else -> "$bytes bytes"
    }

    public fun writeResponse(
        maxBodySize: Int,
        logger: LoggingEventBuilder,
        logTime: Boolean,
        logHeaders: Boolean,
        logBody: Boolean,
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        data: String,
        startTime: Long?
    ) {
        val timeString =
            if (logTime && startTime != null) ("\n=Took         : ${System.currentTimeMillis() - startTime} ms") else ""
        val headersString =
            if (logHeaders) ("\n=Headers      : ${CommonLoggingUtils.INSTANCE.getHeadersContent(response.headers)}") else ""
        val bodyString =
            if (logBody) ("\n=Body         : ${CommonLoggingUtils.INSTANCE.getBodyContent(data, maxBodySize)}") else ""

        val logString = """

===========================SERVER Gateway response begin===========================
=ID           : ${request.id}
=URI          : ${request.method}  ${response.statusCode?.value()} ${getParams(request)}$timeString$headersString$bodyString
===========================SERVER Gateway response end   ==========================""".trimIndent();
        logger.log(logString)
        val logBody = """

===========================SERVER Gateway response begin===========================
=ID           : ${request.id}
=URI          : ${request.method}  ${response.statusCode?.value()} ${getParams(request)}
=Took         : ${if (startTime != null) (System.currentTimeMillis() - startTime) else "NOT_AVAILABLE"} ms
=Headers      : ${
            response.headers.asSequence().map { "${it.key}:${it.value.joinToString(",")}" }.joinToString(";")
        }       
=Body         : $data
===========================SERVER Gateway response end   ==========================""".trimIndent();
        logger.log(logBody)
    }

    public fun writeRequest(
        maxBodySize: Int,
        logger: LoggingEventBuilder,
        logHeaders: Boolean,
        logBody: Boolean,
        request: ServerHttpRequest,
        data: String
    ) {
        val headersString =
            if (logHeaders) ("\n=Headers      : ${CommonLoggingUtils.INSTANCE.getHeadersContent(request.headers)}") else ""
        val bodyString =
            if (logBody) ("\n=Body         : ${CommonLoggingUtils.INSTANCE.getBodyContent(data, maxBodySize)}") else ""
        val logString =
            """
===========================SERVER Gateway request begin===========================
=ID           : ${request.id}
=URI          : ${request.method} ${getParams(request)}$headersString$bodyString
===========================SERVER Gateway request end   ==========================""".trimIndent()

        logger.log(logString)
    }

    companion object {
        @JvmStatic
        val INSTANCE = SpringCloudGatewayLoggingUtils()
    }
}