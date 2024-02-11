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

import io.github.breninsul.webfluxlogging.CommonLoggingUtils
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@ConditionalOnClass(WebClient::class)
@ConditionalOnProperty(
    prefix = "webflux.logging.webclient",
    name = ["disabled"],
    matchIfMissing = true,
    havingValue = "false",
)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@EnableConfigurationProperties(WebClientLoggingProperties::class)
class WebClientLoggingAutoConfig {
    @Bean
    @ConditionalOnMissingBean(CommonLoggingUtils::class)
    fun getCommonLoggingUtils(): CommonLoggingUtils {
        return CommonLoggingUtils()
    }

    @Bean
    @ConditionalOnMissingBean(WebClientLoggingUtils::class)
    fun getWebClientLoggingUtils(
        props: WebClientLoggingProperties,
        commonLoggingUtils: CommonLoggingUtils,
    ): WebClientLoggingUtils {
        val logger = java.util.logging.Logger.getLogger(props.loggerClass)
        return WebClientLoggingUtils(props.maxBodySize, logger, props.getLoggingLevelAsJavaLevel(), props.logTime, props.logHeaders, props.logBody, commonLoggingUtils)
    }

    @Bean
    @ConditionalOnMissingBean(WebClientLoggingExchangeFilterFunction::class)
    fun getWebClientLoggingExchangeFilterFunction(loggingUtils: WebClientLoggingUtils): WebClientLoggingExchangeFilterFunction {
        return WebClientLoggingExchangeFilterFunction(loggingUtils)
    }

    @Bean
    @ConditionalOnMissingBean(WebClient::class)
    fun getWebClientLogging(filter: WebClientLoggingExchangeFilterFunction): WebClient {
        return WebClient
            .builder()
            .filter(filter)
            .build()
    }
}
