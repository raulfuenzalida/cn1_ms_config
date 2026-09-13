package duoc.cn1.ms_config.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.products.base-url:http://localhost:8081}")
    private String productsBaseUrl;

    public void invalidateProductsByFilament(Long idFilament) {
        String url = productsBaseUrl
                + "/api/v1/products/internal/invalidate/filament/"
                + idFilament;

        log.info(
                "Invalidando productos asociados al filamento {} en ms-products",
                idFilament);

        restTemplate.postForEntity(
                url,
                createAuthenticatedEntity(),
                Void.class);
    }

    public void invalidateProductsByPrinting() {
        String url = productsBaseUrl
                + "/api/v1/products/internal/invalidate/printing";

        log.info(
                "Invalidando todos los productos por cambio en configuración de impresión");

        restTemplate.postForEntity(
                url,
                createAuthenticatedEntity(),
                Void.class);
    }

    private HttpEntity<Void> createAuthenticatedEntity() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        HttpHeaders headers = new HttpHeaders();

        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            String accessToken =
                    jwtAuthentication.getToken().getTokenValue();

            headers.setBearerAuth(accessToken);
        }

        return new HttpEntity<>(headers);
    }
}