package ma.fstt.authentificationservice.controller;

import feign.FeignException;
import jakarta.validation.Valid;
import ma.fstt.authentificationservice.dto.MetamaskLoginRequest;
import ma.fstt.authentificationservice.dto.NonceResponse;
import ma.fstt.authentificationservice.dto.TokenResponse;
import ma.fstt.authentificationservice.service.NonceService;
import ma.fstt.authentificationservice.service.SasTokenService;
import ma.fstt.authentificationservice.service.SignatureVerificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/metamask")
public class MetamaskAuthController {

    private final NonceService nonceService;
    private final SignatureVerificationService signatureService;
    private final SasTokenService sasTokenService;

    public MetamaskAuthController(
            NonceService nonceService,
            SignatureVerificationService signatureService,
            SasTokenService sasTokenService) {
        this.nonceService = nonceService;
        this.signatureService = signatureService;
        this.sasTokenService = sasTokenService;
    }

    /**
     * GET /api/auth/metamask/nonce?wallet=0x123...
     * Génère un nonce pour la signature
     */
    @GetMapping("/nonce")
    public ResponseEntity<?> getNonce(@RequestParam String wallet) {
        try {
            String nonce = nonceService.generateAndStoreNonce(wallet);
            return ResponseEntity.ok(Map.of("nonce", nonce));
        } catch (FeignException.NotFound e) {
            // Wallet non trouvé
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", "USER_NOT_FOUND",
                            "message", "Wallet not registered. Please register first."
                    ));
        } catch (FeignException e) {
            // Autres erreurs provenant de User-Management-Service
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "USER_SERVICE_ERROR",
                            "message", "Erreur interne du service utilisateur"
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "INTERNAL_ERROR",
                            "message", "Erreur serveur"
                    ));
        }
    }




    /**
     * POST /api/auth/metamask/login
     * Body: { "wallet": "0x123...", "signature": "0xabc..." }
     *
     * 1. Vérifie la signature
     * 2. Appelle SAS pour obtenir les tokens
     * 3. Retourne les tokens au frontend
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody MetamaskLoginRequest request) {

        // 1. Vérifier la signature MetaMask
        boolean isValid = signatureService.verifySignature(
                request.wallet(),
                request.signature()
        );

        if (!isValid) {
            return ResponseEntity.status(401).build();
        }

        // 2. Demander les tokens au SAS
        TokenResponse tokens = sasTokenService.requestTokens(
                request.wallet(),
                request.signature()
        );

        return ResponseEntity.ok(tokens);
    }

    /**
     * POST /api/auth/metamask/refresh
     * Body: { "refresh_token": "..." }
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @RequestParam String refreshToken) {

        TokenResponse tokens = sasTokenService.refreshTokens(refreshToken);
        return ResponseEntity.ok(tokens);
    }
}