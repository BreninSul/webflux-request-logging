package com.github.breninsul.webfluxlogging.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
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
    fun getWebClientLogging(
        @Value("\${com.github.breninsul.webfluxlogging.logging.max_body_size.web_client:10000}") maxBodySize: Int,
        @Value("\${com.github.breninsul.webfluxlogging.logging.log_time.web_client:true}") logTime: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.log_headers.web_client:true}") logHeaders: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.log_body.web_client:true}") logBody: Boolean,
        @Value("\${com.github.breninsul.webfluxlogging.logging.level.web_client:INFO}") loggingLevel: Level,
        @Value("\${com.github.breninsul.webfluxlogging.logging.logger.web_client:org.springframework.web.reactive.function.client.WebClient}") loggerClass: String,

        ): WebClient {
        val logger: Logger = LoggerFactory.getLogger(loggerClass)

        return WebClient
            .builder()
            .filter(WebClientLoggingExchangeFilterFunction(maxBodySize, logger, loggingLevel,logTime,logHeaders,logBody))
            .build()
    }
}