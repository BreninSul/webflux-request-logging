package com.github.breninsul.webfluxlogging

import org.springframework.http.HttpHeaders

class CommonLoggingUtils protected constructor() {
    public fun getHeadersContent(headers: HttpHeaders) =
        headers.asSequence().map { "${it.key}:${it.value.joinToString(",")}" }.joinToString(";")
    public fun getBodyContent(data: String?, maxBodySize: Int) =
        if (data == null) "<EMPTY>" else if (data.length > maxBodySize) "<TOO BIG ${data.length} bytes>" else data
    companion object {
        @JvmStatic
        val INSTANCE = CommonLoggingUtils()
    }
}