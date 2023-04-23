package com.github.breninsul.webfluxlogging

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders

open class CommonLoggingUtils constructor() {
    public fun getHeadersContent(headers: HttpHeaders) =
        headers.asSequence().map { "${it.key}:${it.value.joinToString(",")}" }.joinToString(";")

    public fun getBodyContent(data: String?, maxBodySize: Int) =
        if (data == null) "<EMPTY>" else if (data.length > maxBodySize) "<TOO BIG ${data.length} bytes>" else data

    public fun getBodyContent(dataBuffer: DataBuffer?, maxBodySize: Int)= getBodyContent(getContent(dataBuffer,maxBodySize), maxBodySize)
    public fun getContent(dataBuffer: DataBuffer?, maxBodySize: Int): String? {
        if (dataBuffer == null) {
            return null;
        }
        val contentLength = dataBuffer.readableByteCount() - dataBuffer.readPosition();
        if (contentLength == 0) {
            return null
        }
        val content = if (contentLength > maxBodySize) "<TOO BIG $contentLength bytes>" else {
            val position = dataBuffer.readPosition()
            val body = String(dataBuffer.asInputStream().readAllBytes())
            dataBuffer.readPosition(position)
            body
        }
        return content
    }
}