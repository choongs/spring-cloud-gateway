package com.spring.cloud.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.MonoProcessor;

/**
 * Created by choonghyun on 2020-01-02.
 */
@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class RoutePredicateFactoriesTests {

  @LocalServerPort
  int port;

  private WebTestClient client;

  @BeforeEach
  void setup() {
    client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).filter(logRequest()).build();
  }

  @Test
  void headerRoute(){
    client.get().uri("/anything").header("EXIST_HEADER", "existHeader")
        .exchange().expectStatus().isOk()
        .expectBody(Map.class)
        .consumeWith(
            result -> {
              assertThat(result.getResponseBody()).isNotEmpty();
              log.info(result.getResponseBody().toString());
            }
        );
  }

  @Test
  void cookieRoute() {

    client.get().uri("/cookies")
        .cookie("EXIST_COOKIE", "existCookie")
        .exchange().expectStatus().isOk()
        .expectBody(Map.class)
        .consumeWith(result -> {
          assertThat(result.getResponseBody()).isNotEmpty();
          log.info(result.getResponseBody().toString());
        });
  }

  @Test
  void hostRoute(){
    client.get().uri("/anything").header("HOST", "www.httpbin.org")
        .exchange().expectStatus().isOk()
        .expectBody(Map.class)
        .consumeWith(
            result -> {
              assertThat(result.getResponseBody()).isNotEmpty();
              log.info(result.getResponseBody().toString());
            }
        );
  }

  @Test
  void httpMethodRoute(){
    client.delete().uri("/delete")
        .exchange().expectStatus().isOk()
        .expectBody(Map.class)
        .consumeWith(
            result -> {
              assertThat(result.getResponseBody()).isNotEmpty();
              log.info(result.getResponseBody().toString());
            }
        );
  }

  @Test
  void pathRoute(){
    client.get().uri("/anything/test")
        .exchange().expectStatus().isOk()
        .expectBody(Map.class)
        .consumeWith(
            result -> {
              assertThat(result.getResponseBody()).isNotEmpty();
              log.info(result.getResponseBody().toString());
            }
        );
  }

  @Test
  void queryRoute(){
    client.get().uri("/get?query=parameter")
        .exchange().expectStatus().isOk()
        .expectBody(Map.class)
        .consumeWith(
            result -> {
              assertThat(result.getResponseBody()).isNotEmpty();
              log.info(result.getResponseBody().toString());
            }
        );
  }

  @Test
  void remoteAddrRoute(){
    client.get().uri("/get")
        .exchange().expectStatus().isOk()
        .expectBody(Map.class)
        .consumeWith(
            result -> {
              assertThat(result.getResponseBody()).isNotEmpty();
              log.info(result.getResponseBody().toString());
            }
        );
  }

  @Test
  void weightAddrRoute(){

    for (int i = 0; i < 10; i++) {
      client.get().uri("/get")
          .exchange()
          .expectBody(Map.class)
          .consumeWith(
              result -> {
                assertThat(result.getResponseBody()).isNotEmpty();
                log.info(result.getResponseBody().toString());
              }
          );
    }

  }

  private static ExchangeFilterFunction logRequest() {

    return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
      log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
      clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
      return MonoProcessor.just(clientRequest);
    });
  }
}
