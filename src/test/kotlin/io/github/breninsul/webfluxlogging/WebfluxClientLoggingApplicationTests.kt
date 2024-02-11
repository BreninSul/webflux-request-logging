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

import io.github.breninsul.webfluxlogging.client.WebClientLoggingAutoConfig
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.*

// @SpringBootApplication
// @SpringBootTest()
// @SpringBootConfiguration()
@ExtendWith(OutputCaptureExtension::class)
@ContextConfiguration(classes = [WebClientLoggingAutoConfig::class])
@WebFluxTest(value = [WebClientLoggingAutoConfig::class])
class WebfluxClientLoggingApplicationTests() {
    @Autowired
    lateinit var webClient: WebClient

    @Test
    fun testWebClientWithoutBody(capturedOutput: CapturedOutput) {
        val testHeader = "TestHeader${UUID.randomUUID()}"
        val uri = "https://www.google.com/"
        val answer =
            webClient
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
    fun testWebClientWithBody(capturedOutput: CapturedOutput) {
        val testBodyValue = "TestBodyValue${UUID.randomUUID()}"
        val testHeader = "TestHeader${UUID.randomUUID()}"
        val uri = "https://www.google.com/"
        val answer =
            webClient
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
