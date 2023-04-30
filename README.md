# Description
This module provides query logging for
1) Spring Cloud Gateway
2) Spring WebFlux WebClient

To connect, simply connect the repository #

Properties (can bes set from system env or application properties)
Spring Cloud Gateway:
<style scoped>
table {
  font-size: 5px;
}
</style>
|                                                Property                                                | Default | Description |
|:------------------------------------------------------------------------------------------------------:| :---: | :---: |
| <sub><sup> com.github.breninsul.webfluxlogging.logging.max_body_size.spring_cloud_gateway </sup></sub> | 10000 | Maximum body size to log |
| <sub><sup> com.github.breninsul.webfluxlogging.logging.add_id_header.spring_cloud_gateway </sup></sub> | TRUE | Add request id to response header |
|  <sub><sup> com.github.breninsul.webfluxlogging.logging.log_time.spring_cloud_gateway    </sup></sub>  | TRUE | Log the elapsed time from the request to the response |
| <sub><sup> com.github.breninsul.webfluxlogging.logging.log_headers.spring_cloud_gateway  </sup></sub>  | TRUE | Log headers |
|  <sub><sup> com.github.breninsul.webfluxlogging.logging.log_body.spring_cloud_gatewa    </sup></sub>   | TRUE | Log body |
|   <sub><sup> com.github.breninsul.webfluxlogging.logging.level.spring_cloud_gateway     </sup></sub>   | INFO | Level for logging messages |
|  <sub><sup> com.github.breninsul.webfluxlogging.logging.logger.spring_cloud_gateway     </sup></sub>   | com.github.breninsul.webfluxlogging.cloud.SpringCloudGatewayLoggingFilter | Class for logger |
|    <sub><sup> com.github.breninsul.webfluxlogging.spring_cloud_gateway.disabled        </sup></sub>    | FALSE | Disable autoconfiguration (register Filter as bean) Spring Cloud Gateway logging filter |

Spring WebFlux WebClient:

|                                           Property                                            | Default | Description |
|:---------------------------------------------------------------------------------------------:| :---: | :---: |
| <sub><sup> com.github.breninsul.webfluxlogging.logging.max_body_size.web_client </sup></sub>  | 10000 | Maximum body size to log |
| <sub><sup> com.github.breninsul.webfluxlogging.logging.log_time.web_client      </sup></sub>  | TRUE | Log the elapsed time from the request to the response |
| <sub><sup> com.github.breninsul.webfluxlogging.logging.log_headers.web_client    </sup></sub> | TRUE | Log headers |
| <sub><sup> com.github.breninsul.webfluxlogging.logging.log_body.web_client      </sup></sub>  | TRUE | Log body |
|  <sub><sup> com.github.breninsul.webfluxlogging.logging.level.web_client        </sup></sub>  | INFO | Level for logging messages |
| <sub><sup> com.github.breninsul.webfluxlogging.logging.logger.web_client        </sup></sub>  | org.springframework.web.reactive.function.client.WebClient | Class for logger |
|  <sub><sup> com.github.breninsul.webfluxlogging.web_client.disabled            </sup></sub>   | FALSE | Disable autoconfiguration (register WebClient with logging filter as bean) Spring WebClient filter |
