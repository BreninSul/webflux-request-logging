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

package com.github.breninsul.webfluxlogging.client

import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.logging.Level

@ConfigurationProperties(prefix = "webflux.logging.webclient")
data class WebClientLoggingProperties(
    var maxBodySize: Int = 10000,
    var logTime: Boolean = true,
    var logHeaders: Boolean = true,
    var logBody: Boolean = true,
    var loggingLevel: String = "INFO",
    var loggerClass: String ="org.springframework.web.reactive.function.client.WebClient",
    var disabled :Boolean = false,
    ){
    fun getLoggingLevelAsJavaLevel():Level{
        return Level.parse(loggingLevel)
    }
}