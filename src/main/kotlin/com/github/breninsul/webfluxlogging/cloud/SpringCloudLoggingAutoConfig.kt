package com.github.breninsul.webfluxlogging.cloud

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.AutoConfigureOrder
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
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.reactive.function.client.WebClient
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
class SpringCloudLoggingAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    fun getWebClientLogging(
        @Value("\${com.github.breninsul.webfluxlogging.logging.max_body_size.spring_cloud_gateway:10000}") maxBodySize: Int,
        @Value("\${com.github.breninsul.webfluxlogging.logging.add_id_header.spring_cloud_gateway:true}") addIdHeader: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.log_time.spring_cloud_gateway:true}") logTime: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.log_headers.spring_cloud_gateway:true}") logHeaders: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.log_body.spring_cloud_gateway:true}") logBody: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.level.spring_cloud_gateway:INFO}") loggingLevel: Level,
        @Value("\${com.github.breninsul.webfluxlogging.logging.logger.spring_cloud_gateway:com.github.breninsul.webfluxlogging.cloud.SpringCloudGatewayLoggingFilter}") loggerClass: String,

        ): SpringCloudGatewayLoggingFilter {
        val logger: Logger = LoggerFactory.getLogger(loggerClass)
        return SpringCloudGatewayLoggingFilter(maxBodySize,logger,loggingLevel, addIdHeader,logTime,logHeaders,logBody )
    }

    @Bean
    @Order(-1)
    fun getErrorWebExceptionHandler(
        @Value("\${com.github.breninsul.webfluxlogging.logging.max_body_size.spring_cloud_gateway:10000}") maxBodySize: Int,
        @Value("\${com.github.breninsul.webfluxlogging.logging.log_time.spring_cloud_gateway:true}") addIdHeader: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.log_time.spring_cloud_gateway:true}") logTime: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.log_headers.spring_cloud_gateway:true}") logHeaders: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.log_body.spring_cloud_gateway:true}") logBody: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.level.spring_cloud_gateway:INFO}") loggingLevel: Level,
        @Value("\${com.github.breninsul.webfluxlogging.logging.logger.spring_cloud_gateway:com.github.breninsul.webfluxlogging.cloud.SpringCloudGatewayLoggingFilter}") loggerClass: String,
        errorAttributes: ErrorAttributes,
        webProperties: WebProperties, viewResolvers: ObjectProvider<ViewResolver>,
        serverCodecConfigurer: ServerCodecConfigurer, applicationContext: AnnotationConfigReactiveWebServerApplicationContext,
        serverProperties: ServerProperties,
    ): SpringCloudGatewayLoggingErrorWebExceptionHandler {
        val logger: Logger = LoggerFactory.getLogger(loggerClass)
        val handler= SpringCloudGatewayLoggingErrorWebExceptionHandler(maxBodySize,addIdHeader,logTime,logHeaders,logBody,logger.atLevel(loggingLevel),errorAttributes,webProperties.resources,serverProperties.error,applicationContext)
        handler.setViewResolvers(viewResolvers.orderedStream().toList())
        handler.setMessageWriters(serverCodecConfigurer.writers)
        handler.setMessageReaders(serverCodecConfigurer.readers)
        return handler
    }
}