package parkinglot.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import parkinglot.managers.JwtService;
import parkinglot.server.repository.AccountRepository;
import parkinglot.users.Admin;
import parkinglot.users.ParkingAttendant;
import parkinglot.utils.LoginResponse;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin
public class AccountController extends BaseController {

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String user, @RequestParam String pass) {
        return accountRepo.findById(user)
                .filter(acc -> acc.login(user, pass))
                .map(acc -> {
                    String role = "ROLE_USER";
                    if (acc instanceof Admin) role = "ROLE_ADMIN";
                    else if (acc instanceof ParkingAttendant) role = "ROLE_ATTENDANT";
                    
                    String token = jwtService.generateToken(acc.getUserName(), role);
                    return success(new LoginResponse(token, acc));
                })
                .orElse(unauthorized("Invalid credentials"));
    }

    @PostMapping("/hardware-login")
    public ResponseEntity<?> hardwareLogin() {
        String token = jwtService.generateToken("SYSTEM_HARDWARE", "ROLE_ADMIN"); 
        return success(new LoginResponse(token, null));
    }
}
