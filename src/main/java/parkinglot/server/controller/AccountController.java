package parkinglot.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import parkinglot.managers.JwtService;
import parkinglot.server.repository.AccountRepository;
import parkinglot.utils.LoginResponse;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin
public class AccountController {

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestParam String user, @RequestParam String pass) {
        return accountRepo.findById(user)
                .filter(acc -> acc.login(user, pass))
                .map(acc -> {
                    String role = "ROLE_USER"; // Basic role for now
                    String token = jwtService.generateToken(acc.getUserName(), role);
                    return ResponseEntity.ok(new LoginResponse(token, acc));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/hardware-login")
    public ResponseEntity<LoginResponse> hardwareLogin() {
        String token = jwtService.generateToken("SYSTEM_HARDWARE", "ROLE_ADMIN"); 
        return ResponseEntity.ok(new LoginResponse(token, null));
    }
}
