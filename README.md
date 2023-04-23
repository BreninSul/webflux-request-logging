# Description
This module provides query logging for
1) Spring Cloud Gateway
2) Spring WebFlux WebClient

To connect, simply connect the repository #

Properties (can bes set from system env or application properties)
Spring Cloud Gateway:
| Property | Default | Description |
| :---: | :---: | :---: |
| com.github.breninsul.webfluxlogging.logging.max_body_size.spring_cloud_gateway | 10000 | Maximum body size to log |
| com.github.breninsul.webfluxlogging.logging.add_id_header.spring_cloud_gateway | TRUE | Add request id to response header |
| com.github.breninsul.webfluxlogging.logging.log_time.spring_cloud_gateway | TRUE | Log the elapsed time from the request to the response |
| com.github.breninsul.webfluxlogging.logging.log_headers.spring_cloud_gateway | TRUE | Log headers |
| com.github.breninsul.webfluxlogging.logging.log_body.spring_cloud_gatewa | TRUE | Log body |
| com.github.breninsul.webfluxlogging.logging.level.spring_cloud_gateway | INFO | Level for logging messages |
| com.github.breninsul.webfluxlogging.logging.logger.spring_cloud_gateway | com.github.breninsul.webfluxlogging.cloud.SpringCloudGatewayLoggingFilter | Class for logger |

Spring WebFlux WebClient:

| Property | Default | Description |
| :---: | :---: | :---: |
| com.github.breninsul.webfluxlogging.logging.max_body_size.web_client | 10000 | Maximum body size to log |
| com.github.breninsul.webfluxlogging.logging.log_time.web_client | TRUE | Log the elapsed time from the request to the response |
| com.github.breninsul.webfluxlogging.logging.log_headers.web_client | TRUE | Log headers |
| com.github.breninsul.webfluxlogging.logging.log_body.web_client | TRUE | Log body |
| com.github.breninsul.webfluxlogging.logging.level.web_client | INFO | Level for logging messages |
| com.github.breninsul.webfluxlogging.logging.logger.web_client | org.springframework.web.reactive.function.client.WebClient | Class for logger |
