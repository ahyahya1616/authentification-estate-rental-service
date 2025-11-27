package ma.fstt.authentificationservice.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MetamaskLoginRequest(
        @NotBlank
        @Pattern(regexp = "^0x[0-9a-fA-F]{40}$")
        String wallet,

        @NotBlank
        String signature
) {}
