package ma.fstt.authentificationservice.utils;

import ma.fstt.authentificationservice.exception.InvalidSignatureException;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.Arrays;

/**
 * Utilitaires de vérification de signature Ethereum
 */
public class Web3SignatureUtils {

    private static final String ETHEREUM_MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n";

    /**
     * Récupère l'adresse Ethereum depuis une signature ECDSA
     */
    public static String ecRecover(String message, String signature) {
        try {
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

            System.out.println("📌 Message original: " + message);
            System.out.println("📌 Message bytes length: " + messageBytes.length);
            System.out.println("📌 Signature: " + signature);

            byte[] signatureBytes = Numeric.hexStringToByteArray(signature);

            if (signatureBytes.length != 65) {
                throw new InvalidSignatureException("Signature invalide : longueur incorrecte");
            }

            byte v = signatureBytes[64];
            System.out.println("📌 V original: " + v);

            if (v < 27) {
                v += 27;
            }

            System.out.println("📌 V ajusté: " + v);

            byte[] r = Arrays.copyOfRange(signatureBytes, 0, 32);
            byte[] s = Arrays.copyOfRange(signatureBytes, 32, 64);

            Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);

            // Essayer avec signedPrefixedMessageToKey
            BigInteger publicKey = Sign.signedPrefixedMessageToKey(
                    messageBytes,
                    signatureData
            );

            String recoveredAddress = "0x" + Keys.getAddress(publicKey);
            System.out.println("📌 Adresse récupérée: " + recoveredAddress);

            return recoveredAddress;

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            throw new InvalidSignatureException("Erreur lors de la récupération de l'adresse : " + e.getMessage());
        }
    }
    /**
     * Vérifie si une adresse Ethereum est valide
     */
    public static boolean isValidAddress(String address) {
        if (address == null || address.isEmpty()) {
            return false;
        }
        return address.matches("^0x[0-9a-fA-F]{40}$");
    }
}