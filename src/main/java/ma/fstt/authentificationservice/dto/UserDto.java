package ma.fstt.authentificationservice.dto;

import java.util.List;

/**
 * DTO représentant un utilisateur
 */
public record UserDto(
        Long id,
        String wallet,
        String username,
        List<String> roles,
        boolean enabled
) {}