# Description

This module provides query logging for

1) Spring Cloud Gateway
2) Spring WebFlux WebClient

To connect, just connect the repository
````
```
repositories {
    mavenCentral()
    ....
   maven {
        name = "GitHub"
        url = uri("https://maven.pkg.github.com/BreninSul/webflux-request-logging")
        credentials {
            username = "GIHUB_PACKAGE_USERNAME" // GIHUB_PACKAGE_USERNAME - your github personal access token username.
            password = "GIHUB_PACKAGE_TOKEN" // GIHUB_PACKAGE_TOKEN - your github personal access token.
        }
    }
    ...
}
```
````

And add lib as dependency
````
```
dependencies {
    ...
    implementation("com.github.breninsul:webflux-logging:1.1.01")
    ...
}

```
````

Properties (can bes set from system env or application properties)

Spring Cloud Gateway:
|                                                          Property                                                          |                                             Default                                             |                                                  Description                                                  |
|:--------------------------------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------------------------:|
| <sub><sup><sub><sup>webflux.logging.gateway.max-body-size</sup></sub></sup></sub> | <sub><sup>                                  10000                                  </sup></sub> | <sub><sup>                               Maximum body size to log                                </sup></sub> |
| <sub><sup><sub><sup>webflux.logging.gateway.add-id-header</sup></sub></sup></sub> | <sub><sup>                                  TRUE                                   </sup></sub> | <sub><sup>                           Add request id to response header                           </sup></sub> |
| <sub><sup><sub><sup>webflux.logging.gateway.log-time    </sup></sub></sup></sub> | <sub><sup>                                  TRUE                                   </sup></sub> | <sub><sup>                 Log the elapsed time from the request to the response                 </sup></sub> |
| <sub><sup><sub><sup>webflux.logging.gateway.log-headers  </sup></sub></sup></sub> | <sub><sup>                                  TRUE                                   </sup></sub> | <sub><sup>                                      Log headers                                      </sup></sub> |
| <sub><sup><sub><sup>webflux.logging.gateway.log-body    </sup></sub></sup></sub> | <sub><sup>                                  TRUE                                   </sup></sub> | <sub><sup>                                       Log body                                        </sup></sub> |
| <sub><sup><sub><sup>webflux.logging.gateway.level     </sup></sub></sup></sub> | <sub><sup>                                  INFO                                   </sup></sub> | <sub><sup>                              Level for logging messages                               </sup></sub> |
| <sub><sup><sub><sup>webflux.logging.gateway.logger     </sup></sub></sup></sub> | <sub><sup>com.github.breninsul.webfluxlogging.cloud.SpringCloudGatewayLoggingFilter</sup></sub> | <sub><sup>                                   Class for logger                                    </sup></sub> |
| <sub><sup><sub><sup>webflux.logging.gateway.disabled        </sup></sub></sup></sub> | <sub><sup>                                  FALSE                                  </sup></sub> | <sub><sup>Disable autoconfiguration (register Filter as bean) Spring Cloud Gateway logging filter</sup></sub> |

Spring WebFlux WebClient:
|                                                      Property                                                       |                                     Default                                      |                                                         Description                                                         |
|:-------------------------------------------------------------------------------------------------------------------:|:--------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------------------------------------:|
| <sub><sup><sub><sup>webflux.logging.webclient.max-body-size.web_client  </sup></sub></sup></sub>  | <sub><sup>                         10000                            </sup></sub> | <sub><sup>                                      Maximum body size to log                                      </sup></sub>  |
| <sub><sup><sub><sup>webflux.logging.webclient.log-time.web_client       </sup></sub></sup></sub>  | <sub><sup>                          TRUE                            </sup></sub> | <sub><sup>                       Log the elapsed time from the request to the response                        </sup></sub>  |
| <sub><sup><sub><sup>webflux.logging.webclient.log-headers.web_client    </sup></sub></sup></sub>  | <sub><sup>                          TRUE                            </sup></sub> | <sub><sup>                                            Log headers                                              </sup></sub> |
| <sub><sup><sub><sup>webflux.logging.webclient.log-body.web_client       </sup></sub></sup></sub>  | <sub><sup>                          TRUE                            </sup></sub> | <sub><sup>                                              Log body                                              </sup></sub>  |
| <sub><sup><sub><sup>webflux.logging.webclient.level         </sup></sub></sup></sub>  | <sub><sup>                          INFO                            </sup></sub> | <sub><sup>                                     Level for logging messages                                     </sup></sub>  |
| <sub><sup><sub><sup>webflux.logging.webclient.logger         </sup></sub></sup></sub>  | <sub><sup>org.springframework.web.reactive.function.client.WebClient</sup></sub> | <sub><sup>                                          Class for logger                                          </sup></sub>  |
| <sub><sup><sub><sup>webflux.logging.webclient.disabled               </sup></sub></sup></sub>  | <sub><sup>                         FALSE                            </sup></sub> | <sub><sup> Disable autoconfiguration (register WebClient with logging filter as bean) Spring WebClient filter </sup></sub>  |
