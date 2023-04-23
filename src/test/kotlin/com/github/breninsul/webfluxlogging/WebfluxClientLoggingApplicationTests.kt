package com.github.breninsul.webfluxlogging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.github.breninsul.webfluxlogging.client.WebClientLoggingAutoConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.*


//@SpringBootApplication
//@SpringBootTest()
//@SpringBootConfiguration()
@ExtendWith(OutputCaptureExtension::class)
@ContextConfiguration(classes = arrayOf(WebClientLoggingAutoConfig::class))
@WebFluxTest(value = arrayOf(WebClientLoggingAutoConfig::class))
class WebfluxClientLoggingApplicationTests() {
    private var appLogger: Logger? = null
    private var listAppender: ListAppender<ILoggingEvent>? = null

    @BeforeEach
    fun setUp() {
        appLogger = LoggerFactory.getLogger(WebClient::class.java)
        listAppender = ListAppender()
        listAppender!!.start()
//        appLogger.addHandler(listAppender)
    }

    @AfterEach
    fun tearDown() {
//        appLogger.detachAppender(listAppender)
    }

    @Autowired
    lateinit var webClient: WebClient

    @Test
    fun testWebClientWithoutBody( capturedOutput:CapturedOutput ) {
        val testHeader = "TestHeader${UUID.randomUUID()}"
        val uri = "https://www.google.com/"
        val answer = webClient
            .post()
            .uri("https://www.google.com/")
            .header("test", testHeader)
            .exchangeToMono { it -> it.bodyToMono<String>() }
            .block()
        val output = capturedOutput.all
        Assertions.assertTrue(output.contains(uri))
        Assertions.assertTrue(output.contains(testHeader))
        Assertions.assertTrue(output.contains(answer!!))
    }
    @Test
    fun testWebClientWithBody( capturedOutput:CapturedOutput ) {
        val testBodyValue = "TestBodyValue${UUID.randomUUID()}"
        val testHeader = "TestHeader${UUID.randomUUID()}"
        val uri = "https://www.google.com/"
        val answer = webClient
            .post()
            .uri(uri)
            .header("test", testHeader)
            .bodyValue(testBodyValue)
            .exchangeToMono { it -> it.bodyToMono<String>() }
            .block()
        val output = capturedOutput.all
        Assertions.assertTrue(output.contains(testHeader))
        Assertions.assertTrue(output.contains(uri))
        Assertions.assertTrue(output.contains(testBodyValue))
        Assertions.assertTrue(output.contains(answer!!))
    }
}
