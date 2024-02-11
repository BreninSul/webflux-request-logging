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

import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.logging.Level
/**
 * WebClientLoggingProperties includes several configurable properties related to WebClient logging level,
 * such as maximum body size, whether to log headers, body, and time, logging level, logger class, and
 * whether logging is disabled.
 *
 * @property maxBodySize the maximum body size to be logged.
 * @property logTime a flag indicating whether the time should be logged.
 * @property logHeaders a flag indicating whether the headers should be logged.
 * @property logBody a flag indicating whether the body should be logged.
 * @property loggingLevel the logging level.
 * @property loggerClass the logger class to be used for logging.
 * @property disabled a flag indicating whether the logging is disabled.
 */
@ConfigurationProperties(prefix = "webflux.logging.webclient")
data class WebClientLoggingProperties(
    var maxBodySize: Int = 10000,
    var logTime: Boolean = true,
    var logHeaders: Boolean = true,
    var logBody: Boolean = true,
    var loggingLevel: String = "INFO",
    var loggerClass: String = "org.springframework.web.reactive.function.client.WebClient",
    var disabled: Boolean = false,
) {
    /**
     * Converts the logging level string into java.util.logging.Level.
     *
     * @return java.util.logging.Level object representing the logging level.
     */
    fun getLoggingLevelAsJavaLevel(): Level {
        return Level.parse(loggingLevel)
    }
}