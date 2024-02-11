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

package io.github.breninsul.webfluxlogging.cloud

import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.logging.Level

/**
 * Class for managing Spring Cloud Gateway logging properties.
 *
 * @property maxBodySize Maximum size of the body that will be logged. Default is 10000.
 * @property addIdHeader Boolean flag to decide if an identification header should be added. Default is true.
 * @property logTime Boolean flag to decide if logging the time is needed. Default is true.
 * @property logHeaders Boolean flag to decide if logging the headers is needed. Default is true.
 * @property logBody Boolean flag to decide if logging the body is needed. Default is true.
 * @property loggingLevel Level of logging. Default is "INFO".
 * @property loggerClass Logger class to be used for logging. Default is "io.github.breninsul.webfluxlogging.cloud.SpringCloudGatewayLoggingFilter".
 * @property disabled Boolean flag to switch off the logging. Default is false.
 *
 * @constructor Create a new logging properties object.
 */
@ConfigurationProperties(prefix = "webflux.logging.gateway")
data class SpringCloudGatewayLoggingProperties(
    var maxBodySize: Int = 10000,
    var addIdHeader: Boolean = true,
    var logTime: Boolean = true,
    var logHeaders: Boolean = true,
    var logBody: Boolean = true,
    var loggingLevel: String = "INFO",
    var loggerClass: String = "io.github.breninsul.webfluxlogging.cloud.SpringCloudGatewayLoggingFilter",
    var disabled: Boolean = false,
) {
    /**
     * This function is used to get the logging level as Java level.
     *
     * @return The logging level as Java Level object.
     */
    fun getLoggingLevelAsJavaLevel(): Level {
        return Level.parse(loggingLevel)
    }
}
