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

import com.github.breninsul.webfluxlogging.CommonLoggingUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@ConditionalOnClass(WebClient::class)
@ConditionalOnProperty(
    prefix = "com.github.breninsul.webfluxlogging.web_client",
    name = arrayOf("disabled"),
    matchIfMissing = true,
    havingValue = "false"
)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
class WebClientLoggingAutoConfig {
    @Bean
    @ConditionalOnMissingBean
    fun getCommonLoggingUtils(
    ): CommonLoggingUtils {
        return CommonLoggingUtils()
    }

    @Bean
    @ConditionalOnMissingBean
    fun getWebClientLoggingUtils(
        @Value("\${com.github.breninsul.webfluxlogging.logging.max_body_size.web_client:10000}") maxBodySize: Int,
        @Value("\${com.github.breninsul.webfluxlogging.logging.log_time.web_client:true}") logTime: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.log_headers.web_client:true}") logHeaders: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.log_body.web_client:true}") logBody: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.level.web_client:INFO}") loggingLevel: java.util.logging.Level,
        @Value("\${com.github.breninsul.webfluxlogging.logging.logger.web_client:org.springframework.web.reactive.function.client.WebClient}") loggerClass: String,
        commonLoggingUtils: CommonLoggingUtils
    ): WebClientLoggingUtils {
        val logger=java.util.logging.Logger.getLogger(loggerClass)
//        val logger = LoggerFactory.getLogger(loggerClass).atLevel(loggingLevel)
        return WebClientLoggingUtils(maxBodySize, logger,loggingLevel, logTime, logHeaders, logBody, commonLoggingUtils)
    }

    @Bean
    @ConditionalOnMissingBean
    fun getWebClientLoggingExchangeFilterFunction(
        loggingUtils: WebClientLoggingUtils
    ): WebClientLoggingExchangeFilterFunction {
        return WebClientLoggingExchangeFilterFunction(loggingUtils)
    }

    @Bean
    @ConditionalOnMissingBean
    fun getWebClientLogging(
        filter: WebClientLoggingExchangeFilterFunction
    ): WebClient {
        return WebClient
            .builder()
            .filter(filter)
            .build()
    }
}