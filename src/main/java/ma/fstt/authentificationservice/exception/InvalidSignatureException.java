package ma.fstt.authentificationservice.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception levée si la signature MetaMask est invalide
 */
public class InvalidSignatureException extends AuthenticationException {

    public InvalidSignatureException(String message) {
        super(message);
    }

    public InvalidSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}