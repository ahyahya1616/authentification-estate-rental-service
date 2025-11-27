package ma.fstt.authentificationservice.service;

import ma.fstt.authentificationservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign Client pour communiquer avec UserManagementService
 */
@FeignClient(name = "user-management-service", url = "http://localhost:8081", path = "/api/users")
public interface UserServiceClient {

    @GetMapping("/wallet/{wallet}")
    UserDto getUserByWallet(@PathVariable String wallet);

    @PostMapping("/nonce")
    void storeNonce(@RequestParam String wallet, @RequestParam String nonce);

    @GetMapping("/nonce")
    String getNonce(@RequestParam String wallet);

    @DeleteMapping("/nonce")
    void deleteNonce(@RequestParam String wallet);


}