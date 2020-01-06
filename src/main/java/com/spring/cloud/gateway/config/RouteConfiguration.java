package com.spring.cloud.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

/**
 * Created by choonghyun on 2020-01-02.
 */
@Configuration
public class RouteConfiguration {

  private final String HTTP_URL = "http://httpbin.org";
  private final String HTTPS_URL = "https://httpbin.org";

  @Bean
  public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {

    //@formatter:off
    return builder.routes()
/*
                  // 현재시간부터
                  .route("after-route", r -> r.after(ZonedDateTime.now(ZoneId.systemDefault())).uri(HTTP_URL))
                  //현재시간까지
                  .route("before-route", r -> r.before(ZonedDateTime.now(ZoneId.systemDefault()).plusHours(1)).uri(HTTP_URL))
                  //from ~ to time
                  .route("between-route", r -> r.between(
                      ZonedDateTime.now(ZoneId.systemDefault()).minusHours(1),
                      ZonedDateTime.now(ZoneId.systemDefault()).plusHours(1)).uri(HTTP_URL))
*/
                  //동일쿠키값
                  .route("cookie-route", r -> r.path("/cookies")
                      .and()
                      .cookie("EXIST_COOKIE", "existCookie1")
                      .uri(HTTP_URL)
                  )
                  //동일헤더값
                  .route("header-route", r -> r.path("/anything")
                      .and()
                      .header("EXIST_HEADER", "existHeader").uri(HTTP_URL)
                  )
                  //HOST
                  .route("host-route", r -> r.host("*.httpbin.org").uri(HTTP_URL))
                  //Http Method
                  .route("method-route", r -> r.method(HttpMethod.DELETE).uri(HTTP_URL))
                  //path
//                  .route("path-route", r -> r.path("/anything/{segment}").uri(HTTP_URL))
                  //query parameter
                  .route("queryParam-route", r -> r.query("query", "parameter").uri(HTTP_URL))
                  //remote address
//                  .route("remoteAddr-route", r -> r.remoteAddr("127.0.0.1").uri(HTTP_URL))
                  // weight lb
//                  .route("weight-route1", r -> r.weight("group1", 5).and().path("/get").uri(HTTP_URL))
//                  .route("weight-route2", r-> r.weight("group1", 5).and().path("/get").uri(HTTPS_URL))
        .build();
    //@formatter:on
  }
}
