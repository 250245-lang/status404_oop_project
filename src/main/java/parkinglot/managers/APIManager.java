package parkinglot.managers;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import parkinglot.constants.VehicleType;
import parkinglot.models.ParkingLot;
import parkinglot.models.ParkingRate;
import parkinglot.models.ParkingTicket;
import parkinglot.users.Account;
import parkinglot.users.Person;
import parkinglot.utils.LoginResponse;
import java.util.Arrays;
import java.util.List;

public class APIManager {
    private String authToken = null;
    private ServerAddress serverAddress;
    private final RestTemplate restTemplate = new RestTemplate();
    private AppContext appContext;

    public APIManager(){
        this.serverAddress = new ServerAddress("127.0.0.1", 8080);
        setupInterceptors();
    }

    public void setAppContext(AppContext context) {
        this.appContext = context;
    }

    private void setupInterceptors() {
        restTemplate.getInterceptors().add((request, body, execution) -> {
            if (authToken != null && !authToken.isEmpty()) {
                request.getHeaders().setBearerAuth(authToken);
            }
            return execution.execute(request, body);
        });
    }

    public void syncData() {
        if (appContext == null) return;
        try {
            ParkingLot lot = restTemplate.getForObject(serverAddress + "/api/parking/status", ParkingLot.class);
            if (lot != null) {
                javafx.application.Platform.runLater(() -> appContext.setParkingLot(lot));
            }
        } catch (Exception e) {
            System.err.println("Sync failed: " + e.getMessage());
        }
    }

    public List<Account> getAccounts() {
        Account[] accounts = restTemplate.getForObject(serverAddress + "/api/accounts", Account[].class);
        return accounts != null ? Arrays.asList(accounts) : List.of();
    }

    public Account getCurrentAccount() {
        return restTemplate.getForObject(serverAddress + "/api/accounts/me", Account.class);
    }

    public void deleteAccount(String username) {
        restTemplate.delete(serverAddress + "/api/accounts/" + username);
    }

    public void updatePassword(String username, String newPassword) {
        String url = UriComponentsBuilder.fromUriString(serverAddress + "/api/accounts/" + username + "/password")
                .queryParam("pass", newPassword)
                .toUriString();
        restTemplate.postForObject(url, null, Void.class);
    }

    public void changePassword(String newPassword) {
        String url = UriComponentsBuilder.fromUriString(serverAddress + "/api/accounts/me/password")
                .queryParam("pass", newPassword)
                .toUriString();
        restTemplate.postForObject(url, null, Void.class);
    }

    public void updatePerson(String username, Person person) {
        restTemplate.postForObject(serverAddress + "/api/accounts/" + username + "/person", person, Void.class);
    }

    public void deleteFloor(String floorName) {
        restTemplate.delete(serverAddress + "/api/parking/floors/" + floorName);
    }

    public void deleteSpot(String floorName, String spotNumber) {
        restTemplate.delete(serverAddress + "/api/parking/floors/" + floorName + "/spots/" + spotNumber);
    }

    public void deleteTicket(String ticketNumber) {
        restTemplate.delete(serverAddress + "/api/parking/tickets/" + ticketNumber);
    }

    public ParkingTicket issueTicket(String license, VehicleType type) {
        String url = UriComponentsBuilder.fromUriString(serverAddress + "/api/parking/issue-ticket")
                .queryParam("license", license)
                .queryParam("type", type)
                .toUriString();
        return restTemplate.postForObject(url, null, ParkingTicket.class);
    }

    public ParkingTicket getTicket(String ticketNumber) {
        return restTemplate.getForObject(serverAddress + "/api/parking/tickets/" + ticketNumber, ParkingTicket.class);
    }

    public void startCharging(String spotNumber) {
        restTemplate.postForObject(serverAddress + "/api/parking/charging/" + spotNumber + "/start", null, Void.class);
    }

    public void stopCharging(String spotNumber) {
        restTemplate.postForObject(serverAddress + "/api/parking/charging/" + spotNumber + "/stop", null, Void.class);
    }

    public void updateRates(ParkingRate rates) {
        restTemplate.postForObject(serverAddress + "/api/parking/rates", rates, Void.class);
    }

    public double calculateFee(String ticketNumber) {
        return restTemplate.getForObject(serverAddress + "/api/parking/calculate-fee/" + ticketNumber, Double.class);
    }

    public String payTicket(String ticketNumber, double amount, String method) {
        String url = UriComponentsBuilder.fromUriString(serverAddress + "/api/parking/pay")
                .queryParam("ticketNumber", ticketNumber)
                .queryParam("amount", amount)
                .queryParam("method", method)
                .toUriString();
        return restTemplate.postForObject(url, null, String.class);
    }

    public void exitVehicle(String ticketNumber) {
        String url = UriComponentsBuilder.fromUriString(serverAddress + "/api/parking/exit")
                .queryParam("ticketNumber", ticketNumber)
                .toUriString();
        restTemplate.postForObject(url, null, String.class);
    }

    public Account login(String username, String password, boolean rememberMe) throws Exception {
        String url = UriComponentsBuilder.fromUriString(serverAddress + "/api/accounts/login")
                .queryParam("user", username)
                .queryParam("pass", password)
                .toUriString();
        LoginResponse response = restTemplate.postForObject(url, null, LoginResponse.class);
        if (response != null) {
            this.authToken = response.token();
            return response.user();
        }
        return null;
    }

    public ServerAddress getServerAddress() { return serverAddress; }
    public void clearToken(){ this.authToken = null; }
    public boolean isLoggedIn() { return authToken != null && !authToken.isEmpty(); }
    public String checkHealth() { return restTemplate.getForObject(serverAddress + "/health", String.class); }
}
