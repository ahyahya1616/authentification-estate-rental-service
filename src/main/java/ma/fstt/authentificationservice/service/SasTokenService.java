package ma.fstt.authentificationservice.service;

import ma.fstt.authentificationservice.dto.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

/**
 * Service pour communiquer avec le Spring Authorization Server (SAS)
 * et obtenir les tokens OAuth2
 */
@Service
public class SasTokenService {

    private static final Logger log = LoggerFactory.getLogger(SasTokenService.class);

    @Value("${app.sas.token-endpoint}")
    private String tokenEndpoint;

    @Value("${app.oauth2.client.id}")
    private String clientId;

    @Value("${app.oauth2.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;

    public SasTokenService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Demande des tokens au SAS via le custom grant_type "metamask"
     */
    public TokenResponse requestTokens(String wallet, String signature) {
        log.info("Demande de tokens au SAS pour wallet: {}", wallet);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "metamask");
        body.add("wallet", wallet);
        body.add("signature", signature);
        body.add("scope", "openid profile read write");

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenEndpoint,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();

            return new TokenResponse(
                    (String) responseBody.get("access_token"),
                    (String) responseBody.get("refresh_token"),
                    (String) responseBody.get("token_type"),
                    ((Number) responseBody.get("expires_in")).longValue()
            );

        } catch (Exception e) {
            log.error("Erreur lors de la demande de tokens au SAS", e);
            throw new RuntimeException("Échec de l'authentification OAuth2", e);
        }
    }

    /**
     * Rafraîchit les tokens via le refresh_token
     */
    public TokenResponse refreshTokens(String refreshToken) {
        log.info("Rafraîchissement des tokens");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenEndpoint,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();

            return new TokenResponse(
                    (String) responseBody.get("access_token"),
                    (String) responseBody.get("refresh_token"),
                    (String) responseBody.get("token_type"),
                    ((Number) responseBody.get("expires_in")).longValue()
            );

        } catch (Exception e) {
            log.error("Erreur lors du rafraîchissement des tokens", e);
            throw new RuntimeException("Échec du rafraîchissement des tokens", e);
        }
    }
}