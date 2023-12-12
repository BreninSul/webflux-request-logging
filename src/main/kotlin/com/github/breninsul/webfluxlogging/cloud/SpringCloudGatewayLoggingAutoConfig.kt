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

package com.github.breninsul.webfluxlogging.cloud

import com.github.breninsul.webfluxlogging.CommonLoggingUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.reactive.result.view.ViewResolver

@Configuration
@ConditionalOnClass(GatewayFilter::class)
@ConditionalOnProperty(
    prefix = "com.github.breninsul.webfluxlogging.spring_cloud_gateway",
    name = arrayOf("disabled"),
    matchIfMissing = true,
    havingValue = "false"
)
@AutoConfigureBefore(ErrorWebFluxAutoConfiguration::class)
class SpringCloudGatewayLoggingAutoConfig {
    @Bean
    @ConditionalOnMissingBean
    fun getCommonLoggingUtils(
    ): CommonLoggingUtils {
        return CommonLoggingUtils()
    }
    @Bean
    @ConditionalOnMissingBean
    fun getSpringCloudGatewayLoggingUtils(
        @Value("\${com.github.breninsul.webfluxlogging.logging.max_body_size.spring_cloud_gateway:10000}") maxBodySize: Int,
        @Value("\${com.github.breninsul.webfluxlogging.logging.add_id_header.spring_cloud_gateway:true}") addIdHeader: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.log_time.spring_cloud_gateway:true}") logTime: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.log_headers.spring_cloud_gateway:true}") logHeaders: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.log_body.spring_cloud_gateway:true}") logBody: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.level.spring_cloud_gateway:INFO}") loggingLevel: java.util.logging.Level,
        @Value("\${com.github.breninsul.webfluxlogging.logging.logger.spring_cloud_gateway:com.github.breninsul.webfluxlogging.cloud.SpringCloudGatewayLoggingFilter}") loggerClass: String,
        commonLoggingUtils: CommonLoggingUtils
        ): SpringCloudGatewayLoggingUtils {
        val logger=java.util.logging.Logger.getLogger(loggerClass)
//        val logger = LoggerFactory.getLogger(loggerClass).atLevel(loggingLevel)
        return SpringCloudGatewayLoggingUtils(maxBodySize,logger,loggingLevel,logTime,logHeaders,logBody,commonLoggingUtils)
    }

    @Bean
    @ConditionalOnMissingBean
    fun getSpringCloudGatewayLoggingFilter(
        @Value("\${com.github.breninsul.webfluxlogging.logging.add_id_header.spring_cloud_gateway:true}") addIdHeader: Boolean,
        springCloudGatewayLoggingUtils: SpringCloudGatewayLoggingUtils,
        ): SpringCloudGatewayLoggingFilter {
        return SpringCloudGatewayLoggingFilter(addIdHeader,springCloudGatewayLoggingUtils )
    }

    @Bean
    @Order(-1)
    fun getSpringCloudGatewayLoggingErrorWebExceptionHandler(
        @Value("\${com.github.breninsul.webfluxlogging.logging.max_body_size.spring_cloud_gateway:10000}") maxBodySize: Int,
        @Value("\${com.github.breninsul.webfluxlogging.logging.log_time.spring_cloud_gateway:true}") addIdHeader: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.log_time.spring_cloud_gateway:true}") logTime: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.log_headers.spring_cloud_gateway:true}") logHeaders: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.log_body.spring_cloud_gateway:true}") logBody: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.level.spring_cloud_gateway:INFO}") loggingLevel: Level,
        @Value("\${com.github.breninsul.webfluxlogging.logging.logger.spring_cloud_gateway:com.github.breninsul.webfluxlogging.cloud.SpringCloudGatewayLoggingFilter}") loggerClass: String,
        errorAttributes: ErrorAttributes,
        webProperties: WebProperties,
        viewResolvers: ObjectProvider<ViewResolver>,
        serverCodecConfigurer: ServerCodecConfigurer,
        applicationContext: AnnotationConfigReactiveWebServerApplicationContext,
        serverProperties: ServerProperties,
        utils: SpringCloudGatewayLoggingUtils,
        ): SpringCloudGatewayLoggingErrorWebExceptionHandler {
        val handler= SpringCloudGatewayLoggingErrorWebExceptionHandler(addIdHeader,utils,errorAttributes,webProperties.resources,serverProperties.error,applicationContext)
        handler.setViewResolvers(viewResolvers.orderedStream().toList())
        handler.setMessageWriters(serverCodecConfigurer.writers)
        handler.setMessageReaders(serverCodecConfigurer.readers)
        return handler
    }
}