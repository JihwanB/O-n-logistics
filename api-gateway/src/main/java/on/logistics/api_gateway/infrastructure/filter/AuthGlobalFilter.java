package on.logistics.api_gateway.infrastructure.filter;

import lombok.extern.slf4j.Slf4j;
import on.logistics.api_gateway.global.presentation.dtos.CommonResponse;
import on.logistics.api_gateway.presentation.dtos.AuthValidateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j(topic = "AuthGlobalFilter")
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final WebClient webClient;
    @Value("${spring.cloud.gateway.auth.login.endpoint}")
    private String loginUrl;
    @Value("${spring.cloud.gateway.auth.signup.endpoint}")
    private String signupUrl;
    @Value("${spring.cloud.gateway.auth.validate.endpoint}")
    private String validateEndpoint;
    @Value("${spring.cloud.gateway.auth.secret.key}")
    private String secretKey;

    public AuthGlobalFilter(
        WebClient.Builder webClientBuilder,
        @Value("${spring.cloud.gateway.auth.base.url}") String baseUrl
    ) {
        this.webClient = webClientBuilder
            .baseUrl(baseUrl)
            .build();
    }

    @Override
    public Mono<Void> filter(
        ServerWebExchange exchange,
        GatewayFilterChain chain
    ) {
        log.info("Auth Global Filter");
        String path = exchange.getRequest().getURI().getPath();
        String ipAddress = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();

        log.info("Auth Requested Ip address: {}, {}", ipAddress, path);

        if (
            path.equals(loginUrl)  ||
            path.equals(signupUrl)  ||
            path.startsWith("/api-docs-user-service") ||
            path.startsWith("/api-docs-hub-service") ||
            path.startsWith("/api-docs-company-service") ||
            path.startsWith("/api-docs-product-service") ||
            path.startsWith("/api-docs-order-service") ||
            path.startsWith("/api-docs-delivery-service") ||
            path.startsWith("/api-docs-slack-ai-service") ||
            path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/api-docs") ||
            path.contains("swagger") ||
            path.contains("api-docs") ||
            path.contains("spring-doc")
        ) {
            return chain.filter(exchange);
        }

        log.info("Auth Global Filter Accept");
        String accessToken = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (accessToken == null || accessToken.isEmpty()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        var refreshCookie = exchange.getRequest().getCookies().getFirst("refreshToken");
        if (refreshCookie == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        String refreshToken = refreshCookie.getValue();

        log.info("Token Extracted");
        return webClient.get()
            .uri(validateEndpoint)
            .header("Authorization", accessToken)
            .header("X-Internal-Secret", secretKey)
            .cookie("refreshToken", refreshToken)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<CommonResponse<AuthValidateResponse>>() {
            })
            .flatMap(commonResponse -> {
                if (commonResponse.data() == null) {
                    log.error("Common Response Data Is Null");
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
                String passportId = commonResponse.data().passportId();
                log.info("Passport Id Extracted");
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .headers(httpHeaders -> {
                        httpHeaders.remove("Authorization");
                        httpHeaders.add("X-Passport-Id", passportId);
                    })
                    .build();
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            })
            .onErrorResume(ex -> {
                log.error(ex.getMessage());
                log.error("Request URL : {} ", exchange.getRequest().getURI());
                log.error("WebClientInfo : {} ", validateEndpoint);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            });
    }

    @Override
    public int getOrder() {
        return -1;
    }

}
