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