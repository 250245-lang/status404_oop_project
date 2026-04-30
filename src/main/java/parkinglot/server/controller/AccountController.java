package parkinglot.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import parkinglot.managers.JwtService;
import parkinglot.users.Account;
import parkinglot.users.ParkingAttendant;
import parkinglot.server.repository.AccountRepository;
import parkinglot.users.Person;
import parkinglot.utils.LoginResponse;

import java.security.Principal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

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
                    String role = "ROLE_USER";
                    if (acc instanceof parkinglot.users.Admin) role = "ROLE_ADMIN";
                    else if (acc instanceof parkinglot.users.ParkingAttendant) role = "ROLE_ATTENDANT";
                    
                    String token = jwtService.generateToken(acc.getUserName(), role);
                    return ResponseEntity.ok(new LoginResponse(token, acc));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/hardware-login")
    public ResponseEntity<LoginResponse> hardwareLogin() {
        // Generates a system-level token for hardware simulators
        // This allows simulated panels to interact with the API without a human login
        String token = jwtService.generateToken("SYSTEM_HARDWARE", "ROLE_ADMIN"); 
        // We use ROLE_ADMIN for now to ensure they have full access to parking logic
        return ResponseEntity.ok(new LoginResponse(token, null));
    }

    // --- Generalized Account Management ---

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/add")
    public Account addAccount(@RequestBody Account account) {
        return accountRepo.save(account);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/update")
    public ResponseEntity<Account> updateAccount(@RequestBody Account account) {
        return accountRepo.findById(account.getUserName())
                .map(existing -> {
                    existing.setStatus(account.getStatus());
                    existing.setPerson(account.getPerson());
                    
                    // Preserve password if not provided in update
                    if (account.getPassword() != null && !account.getPassword().isEmpty()) {
                        existing.setPassword(account.getPassword());
                    }
                    
                    return ResponseEntity.ok(accountRepo.save(existing));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Deprecated specific endpoints
    @Deprecated
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/add-attendant")
    public Account addAttendant(@RequestBody ParkingAttendant attendant) {
        return accountRepo.save(attendant);
    }

    @Deprecated
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/update-attendant")
    public ResponseEntity<Account> updateAttendant(@RequestBody ParkingAttendant attendant) {
        if (!accountRepo.existsById(attendant.getUserName())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(accountRepo.save(attendant));
    }

    // Admin deletes an account
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{username}")
    public ResponseEntity<String> deleteAccount(@PathVariable String username) {
        if (!accountRepo.existsById(username)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        accountRepo.deleteById(username);
        return ResponseEntity.ok("User deleted successfully");
    }


    @PostMapping("/update-person")
    public ResponseEntity<String> updatePerson(@RequestParam String username, @RequestBody Person person) {
        return accountRepo.findById(username)
                .map(acc -> {
                    acc.setPerson(person);
                    accountRepo.save(acc);
                    return ResponseEntity.ok("Person details updated successfully");
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }

    @GetMapping("/me")
    public ResponseEntity<Account> getCurrentAccount(Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return accountRepo.findById(principal.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(Principal principal, @RequestParam String newPassword) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return accountRepo.findById(principal.getName())
                .map(acc -> {
                    acc.setPassword(newPassword);
                    accountRepo.save(acc);
                    return ResponseEntity.ok("Password changed successfully");
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }

    // 3. List all accounts (For Admin Dashboard)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/all")
    public List<Account> getAll() {
        return accountRepo.findAll();
    }
}