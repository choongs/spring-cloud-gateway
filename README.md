> 개인적인 공부용도로 작성한 내용입니다. 잘 이해가 되지 않는 부분은 원문 그대로 넣었습니다.
> 그렇다고 물론 해석한 부분도 틀릴수가 있습니다...
> 미약하게나마 도움이 되었으면 좋겠습니다.

# Spring Cloud Gateway

> This project provides a library for building an API Gateway on top of Spring MVC. Spring Cloud Gateway aims to provide a simple, yet effective way to route to 
> APIs and provide cross cutting concerns to them such as: security, monitoring/metrics, and resiliency.

## features
* Built on Spring Framework 5, Project Reactor and Spring Boot 2.0
* Able to match routes on any request attribute.
* Predicates and filters are specific to routes.
* Hystrix Circuit Breaker integration.
* Spring Cloud DiscoveryClient integration
* Easy to write Predicates and Filters
* Request Rate Limiting
* Path Rewriting

## spring-cloud-starter-gateway
Spring cloud gateway를 프로젝트 포함 하기위해서는 `org.springframework.cloud:spring-cloud-starter-gateway`를 사용하면됩니다.

> Spring cloud gateway는 Springboot 및 Spring webflux가 제공하는 netty 런타임을 필요로합니다.
> 전통적인 서블릿컨테이너나 WAR로 빌등된경우 동작하지 않습니다.

`org.springframework.cloud:spring-cloud-starter-gateway` 설정한 후 사용하고 싶지 않은 경우에는 application.yml 설정에서 `spring.cloud.gateway.enabled=false`


## 용어

### Route (경로)
게이트웨이의 기본 building block을 라우팅함, building block은 ID, 목적지 URI, 조건부(predicate)의 집합 및 필터들의 집으로 정의.
전제 조건이 true일경우 경로를 매치함.

### Predicate(조건부)
java8에서 제공하는 function Predicate interface. 사용하는 class는 `ServerWebExchange` Http request(Headers, parameter) 어느것이든 매치가 가능하게 도와줌.

### Filter
특정한 factory 생성된 Spring framework GatewayFilter 인스턴스.


### 동작원리
#### Client - Gateway Handler Mappling - Filter - Filter - Procy Filter - Proxy Service

Header Forwarded에 gateway ip 추가되어짐 response시 다시 돌아옴.

## 작성하는방법

Gateway를 작성하는 방법은 2가지입니다. 그 첫번째로는 application.yml에 설정을 통해서 작성할 수 있습니다.
```
spring:
  cloud:
    gateway:
      routes:
      - id: after_route
        uri: https://example.org
        predicates:
        - After=2017-01-20T17:42:47.789-07:00[America/Denver]
```

2번째 방법으로 java source에서 설정이 가능합니다.

```java

  @Bean
  public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
        //After Route Predicate Factory
        .route(
            r -> r.path("/after-route").and()
                .after(LocalDateTime.now().atZone(ZoneId.systemDefault())).uri("http://viewing.kr").id("after-route")
        ).build();
  }

```

## Route Predicate Factories
Spring Cloud Gateway는 `Spring Webflux HandlerMapping`의 한 부분으로 경로를 매치합니다.
Spring Cloud Gateway는 다양한 Route Predicate Factory를 포함하고 있으며,
모든 조건부는 HTTP 다른 속성들과 매치됨과 동시에 복수의 Predicate Factory 와 결합 할 수 있이며 AND 및 OR 조건으로 결합되어집니다.

### After Route Predicate Factory
After Route Predicate Factory는 datetime이라는 하나의 parameter만 가지고, 이 조건부는 현재 datetime보다 이후에 일어난 요청과 매칭됩니다.

```java

.route (
	r -> r.path("/after-route")
    .and()
	.after(LocalDateTime.now().atZone(ZoneId.systemDefault())).uri("http://viewing.kr").id("after-route")
)

```

### Before Route Predicate Factory
Before Route Predicate Factory는 datetime이라는 하나의 parameter만 가지고, 이 조건부는 현재 datetime보다 이전에 일어난 요청과 매칭됩니다.

```java

.route(
	r -> r.path("/before-route").and()
	.before(LocalDateTime.now().atZone(ZoneId.systemDefault()).plusHours(1))
	.uri("http://viewing.kr").id("before-route")
)

```

### Between Route Predicate Factory
Between Route Predicate Factory는 두개의 datetime parameter를 가지고 있으며, datetime1과 datetime2 사이에 일어난 요청과 매칭되어집니다.

``` java
.route(
	r -> r.path("/between-route").and()
		.between(
			LocalDateTime.now().atZone(ZoneId.systemDefault()),
			LocalDateTime.now().atZone(ZoneId.systemDefault()).plusHours(1)
		).uri("http://viewing.kr").id("between-route")

)

```

### Cookie Route Predicate Factory
Cookie Route Predicate Factory는 cookie name과 정규식을 가지고 있으며 조건부는 해당하는 이름과 정규식에 일치하는 값을 가지는 쿠키와 매칭되어집니다.

### Header Route Predicate Factory
Header Route Predicate Factory는 Header name과 정규식을 가지며 조건부는 해당하는 이름과 정규식에 일치하는 값을 가지는 Header와 매칭되어집니다.

### Host Route Predicate Factory
Host Route Predicate Factory는 host name 하나의 parameter 를 가지며 해당 pattern은 .구분자로 가지는 ant style 이다. 조건부는 패턴과 일치하는 host와 매칭되어집니다.

```text
//ant style
? : 1개의 문자와 매칭 (matches single character)

* : 0개 이상의 문자와 매칭 (matches zero or more characters)

** : 0개 이상의 디렉토리와 파일 매칭 (matches all files / directories)

```

### Method Route Predicate Factory
Method Route Predicate Factory는 HTTP Method parameter 하나를 가지며, 조건부는 mehtod동일한지 확인후 매칭되어집니다.

### Path Route Predicate Factory
Path Route Predicate Factory는 필수 parameter인 pathMatcher list와 옵션 ~~matchOptionalTrailingSeparator~~를 가진다.
~~matchOptionalTrailingSeparator~~는 deprecated 됨.

### Query Route Predicate Factory
Query Route Predicate Factory는 parameter name과 정규식을 가지지만 정규식은 옵션값입니다. 조건부는 query param에 parameter가 존재하며 매칭되어집니다.

## GatewayFilter Factories
라우트 필터는 들어오고 나가는 HTTP 요청과 응답을 수정할 수 있게 해줍니다. spring cloud gateway 다양한 GatewayFilter를 포함하고 있습니다.

### AddRequestHeader GatewayFilter Factory
AddRequestHeader GatewayFilter Factory는 header name 과 header value 2가지의 파라메터를 가지고 있으며, 메소드 이름 그대로 Request에 Header 속성을 추가해줍니다.

```java
.route(
	r -> r.path("/add/request/header").and()
		.after(LocalDateTime.now().atZone(ZoneId.systemDefault()))
		.filters(f -> f.addRequestHeader("header-test", "header-value")//add header in request
        ).uri("http://ott-apilocal.cjhello.com:8090").id("addRequestHeader")
)
```

### AddRequestParameter GatewayFilter Factory
AddRequestParameter GatewayFilter Factoryv는 parameter name 과 parameter value 2가지의 파라메터를 가지고 있으며 이름 그대로 Request에 parameter 값을 추가합니다.

```java
.route(
	r -> r.path("/add/request/parameter").and()
		.after(LocalDateTime.now().atZone(ZoneId.systemDefault()))
		.filters(f -> f.addRequestParameter("parameterName", "parameterValue") //add parameter
        ).uri("http://ott-apilocal.cjhello.com:8090").id("addRequestParameter")
)
```


> 테스트중 요청되는 parameter에 `=` 특정 문자열이 들어가니 Exception이 발생함.
> java.lang.IllegalStateException: Invalid URI query:

```java
//AddRequestParameterGatewayFilterFactory.java

try {
	//문제가 되었던 부분 기존 parameter에 추가 parameter적용 후 URI를 생성하다가 Exception이 발생.
	URI newUri = UriComponentsBuilder.fromUri(uri).replaceQuery(query.toString()).build(true).toUri();

	ServerHttpRequest request = exchange.getRequest().mutate().uri(newUri).build();

	return chain.filter(exchange.mutate().request(request).build());
	}catch (RuntimeException ex) {
		throw new IllegalStateException("Invalid URI query: \"" + query.toString() + "\"");
	}

```

### AddResponseHeader GatewayFilter Factory
AddResponseHeader GatewayFilter Factory는 AddRequestHeader GatewayFilter Factory와 동일하게 header값을 설정해주지만 Response에 Header값을 추가합니다.

```java
.route(
	r -> r.path("/add/response/header").and()
		.after(LocalDateTime.now().atZone(ZoneId.systemDefault()))
		.filters(f -> f..addResponseHeader("responseHeader-test", "responseHeader-value"))
        .uri("http://ott-apilocal.cjhello.com:8090").id("addResponseHeader")
)
```

### DedupeResponseHeader GatewayFilter Factory
중복되는 헤더를 제거한다는건데.. 사실 잘모르겠음요..ㅠㅠ

### Hystrix GatewayFilter Factory
Hystrixs는 서킷프레이커 패턴을 구현한 넷플릭스의 라이브러리이며 Hystrix GatewayFilter는 gateway 경로에 서킷브레이커를 도입할 수 있게 하여, 서비스가 실패하는것을 방지하고 
fallback(대비책) 응답을 제공 할 수 있도록 해줍니다.
hystrix를 사용하기위해서는 `spring-cloud-starter-netflix-hystrix` 라이브러리 추가 필요합니다.

> Hystrix는 더이상 기능 추가과 없을 예정이므로, 아래에서 설명할 `Spring Cloud CircuitBreaker GatewayFilter Factory`를 `Resilience4J` 와 함께 사용하기 권장.
> Hystrix는 기능에서 삭제될 예정
> `Resilience4J`는 java8과 함수형 프로그래밍을 지원하고 Vavr 라이브러리만 사용하며 그이외 다른 어떤 외부 의존성이 없는 경량화 라이브러리?

The Hystrix GatewayFilter Factory는 HystrixCommand의 단일이름 가지며, 추가적으로 옵션날한 fallbackUri parameter를 가진다. 현재는 `forward` schemed URI만 제공한다.
기본적으로 fallbackUri는 내부컨트롤러(게이트웨이서버)에 사용하는거지만 외부서버로도 reroute 가능하다.

### Spring Cloud CircuitBreaker GatewayFilter Factory
Spring Cloud CircuitBreaker GatewayFilter Factory를 사용하기위해서는 `spring-cloud-starter-circuitbreaker-reactor-resilience4j` 라이브러리가 필요합니다.
위에서 설명했듯이 Hystrix GatewayFilter Factory보다는 Spring Cloud CircuitBreaker GatewayFilter Factory를 주천합니다.

### FallbackHeaders GatewayFilter Factory
### MapRequestHeader GatewayFilter Factory
### PrefixPath GatewayFilter Factory
### PreserveHostHeader GatewayFilter Factory
### RequestRateLimiter GatewayFilter Factory
### Redis RateLimiter
### RedirectTo GatewayFilter Factory
### RemoveHopByHopHeadersFilter GatewayFilter Factory
### RemoveRequestHeader GatewayFilter Factory
### RemoveResponseHeader GatewayFilter Factory
### RemoveRequestParameter GatewayFilter Factory
### RewritePath GatewayFilter Factory
### RewriteLocationResponseHeader GatewayFilter Factory
### RewriteResponseHeader GatewayFilter Factory
### SaveSession GatewayFilter Factory
### SecureHeaders GatewayFilter Factory
### SetPath GatewayFilter Factory
### SetRequestHeader GatewayFilter Factory
### SetResponseHeader GatewayFilter Factory
### SetStatus GatewayFilter Factory
### StripPrefix GatewayFilter Factory
### Retry GatewayFilter Factory
### RequestSize GatewayFilter Factory
### Modify Request Body GatewayFilter Factory
### Modify Response Body GatewayFilter Factory
### Default Filters



## Global Filters
Global Filters는 GatewayFilter와 동일한 특징을 가지지만 조건에 따라 모든 route들에게 적용될수 있습니다. (전역필터 같은 개념..)


### Combined Global Filter and GatewayFilter Ordering
전역 필터를 하나로 그룹으로 묶을수도있고, 거기에 호출되는 필터의 순서를 정할수있다.
기존 필터들과 동일하게 Spring Cloud Gateway는 pre와 post를 구분한다.


참조 : https://cloud.spring.io/spring-cloud-static/spring-cloud-gateway/2.2.0.RELEASE/reference/html/#gateway-starter


