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

package io.github.breninsul.webfluxlogging

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import java.nio.charset.Charset

/**
 * Utility class for logging HTTP headers and body content.
 */
open class CommonLoggingUtils {

    /**
     * Returns the content of the HTTP headers as a string.
     *
     * @param headers the HTTP headers.
     * @return the headers content as a string.
     */
    open fun getHeadersContent(headers: HttpHeaders) = headers.asSequence().map { "${it.key}:${it.value.joinToString(",")}" }.joinToString(";")

    /**
     * Returns the body content as a string. If the data is null, returns `<EMPTY>`. If the data's size is greater than max body size, then a byte count is returned. Otherwise, the actual data is returned.
     *
     * @param data the body.
     * @param maxBodySize the maximum body size that can be returned.
     * @return the body content as a string.
     */
    open fun getBodyContent(
        data: String?,
        maxBodySize: Int,
    ) = if (data == null) {
        "<EMPTY>"
    } else if (data.length > maxBodySize) {
        "<TOO BIG ${data.length} bytes>"
    } else {
        data
    }

    /**
     * Returns the body content as a string from a data buffer.
     *
     * @param dataBuffer the data buffer.
     * @param maxBodySize the maximum body size that can be returned.
     * @return the body content as a string.
     */
    open fun getBodyContent(
        dataBuffer: DataBuffer?,
        maxBodySize: Int,
    ) = getBodyContent(getContent(dataBuffer, maxBodySize), maxBodySize)

    /**
     * Returns the content bytes from a data buffer.
     *
     * @param dataBuffer the data buffer.
     * @return the content bytes.
     */
    open fun getContentBytes(dataBuffer: DataBuffer?): ByteArray? {
        val contentLength = countContentLength(dataBuffer)
        if (contentLength == 0) {
            return null
        }
        val position = dataBuffer!!.readPosition()
        val body = dataBuffer.asInputStream().readAllBytes()
        dataBuffer.readPosition(position)
        return body
    }

    /**
     * Returns the content from a data buffer as a string.
     *
     * @param dataBuffer the data buffer.
     * @param maxBodySize the maximum body size that can be returned.
     * @param charset the charset to decode the bytes into characters.
     * @return the content as a string.
     */
    open fun getContent(
        dataBuffer: DataBuffer?,
        maxBodySize: Int,
        charset: Charset = Charsets.UTF_8,
    ): String? {
        val contentLength = countContentLength(dataBuffer)
        if (contentLength == 0) {
            return null
        }
        val stringContent =
            if (contentLength > maxBodySize) {
                "<TOO BIG $contentLength bytes>"
            } else {
                getContentBytes(dataBuffer)?.let { String(it, charset) }
            }
        return stringContent
    }

    /**
     * Returns the content length from a data buffer.
     *
     * @param dataBuffer the data buffer.
     * @return the content length of the data buffer.
     */
    open fun countContentLength(dataBuffer: DataBuffer?) =
        if (dataBuffer == null) {
            0
        } else {
            dataBuffer.readableByteCount() - dataBuffer.readPosition()
        }
}