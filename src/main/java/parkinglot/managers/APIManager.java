package parkinglot.managers;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import parkinglot.constants.ParkingSpotType;
import parkinglot.constants.VehicleType;
import parkinglot.models.ParkingFloor;
import parkinglot.models.ParkingLot;
import parkinglot.models.ParkingTicket;
import parkinglot.users.Account;
import parkinglot.users.ParkingAttendant;
import parkinglot.users.Person;
import parkinglot.utils.LoginResponse;

import java.util.ArrayList;
import java.util.List;

public class APIManager {
    private String authToken = null;
    private ServerAddress serverAddress;
    private final RestTemplate restTemplate = new RestTemplate();

    public APIManager(String ip, int port){
        this.serverAddress = new ServerAddress(ip, port);
        loadServerConfig();
        loadTokenFromFile();
        setupInterceptors();
    }

    public APIManager(){
        this.serverAddress = new ServerAddress("127.0.0.1", 8080);
        loadServerConfig();
        loadTokenFromFile();
        setupInterceptors();
    }

    private void setupInterceptors() {
        restTemplate.getInterceptors().add((request, body, execution) -> {
            if (authToken != null && !authToken.isEmpty()) {
                request.getHeaders().setBearerAuth(authToken);
            }
            return execution.execute(request, body);
        });
    }

    public void setServerAddress(String ip, int port){
        serverAddress = new ServerAddress(ip, port);
        saveServerConfig();
    }

    public void setServerIp(String ip){
        setServerAddress(ip, serverAddress.port);
    }
    public void setServerPort(int port){
        setServerAddress(serverAddress.ip, port);
    }

    public ServerAddress getServerAddress() {return serverAddress;}

    public void clearToken(){
        this.authToken = null;
        saveTokenToFile(null);
    }

    public boolean isLoggedIn() {
        return authToken != null && !authToken.isEmpty();
    }

    // 1. Health Check
    public String checkHealth() {
        return restTemplate.getForObject(serverAddress + "/health", String.class);
    }

    public Account login(String username, String password, boolean rememberMe) throws Exception{
        String url = UriComponentsBuilder.fromUriString(serverAddress + "/api/accounts/login")
                .queryParam("user", username)
                .queryParam("pass", password)
                .toUriString();

        try {
            LoginResponse response = restTemplate.postForObject(url, null, LoginResponse.class);
            if (response != null) {
                this.authToken = response.token();
                if (rememberMe) {
                    saveTokenToFile(this.authToken);
                } else {
                    saveTokenToFile(null);
                }
                return response.user();
            }
            return null;
        } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized e) {
            return null;
        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
            throw e;
        }
    }
    public void hardwareLogin() throws Exception {
        String url = serverAddress + "/api/accounts/hardware-login";
        try {
            LoginResponse response = restTemplate.postForObject(url, null, LoginResponse.class);
            if (response != null) {
                this.authToken = response.token();
                // We don't save this to file as it's a transient session for the simulator
            }
        } catch (Exception e) {
            System.err.println("Hardware login failed: " + e.getMessage());
            throw e;
        }
    }


    public void updatePerson(String username, Person person) throws Exception {
        String url = UriComponentsBuilder.fromUriString(serverAddress + "/api/accounts/update-person")
                .queryParam("username", username)
                .toUriString();
        try {
            restTemplate.postForObject(url, person, String.class);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            throw new Exception(e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new Exception("Connection error: " + e.getMessage());
        }
    }

    public Account getCurrentAccount() {
        return restTemplate.getForObject(serverAddress + "/api/accounts/me", Account.class);
    }

    public void changePassword(String newPassword) throws Exception {
        String url = UriComponentsBuilder.fromUriString(serverAddress + "/api/accounts/change-password")
                .queryParam("newPassword", newPassword)
                .toUriString();
        try {
            restTemplate.postForObject(url, null, String.class);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            throw new Exception(e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new Exception("Connection error: " + e.getMessage());
        }
    }

    public List<Account> getAllAccounts() throws Exception {
        try {
            Account[] accounts = restTemplate.getForObject(serverAddress + "/api/accounts/all", Account[].class);
            return accounts != null ? List.of(accounts) : new ArrayList<>();
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            throw new Exception(extractErrorMessage(e));
        } catch (Exception e) {
            throw new Exception("Connection error: " + e.getMessage());
        }
    }

    public void addAccount(Account account) throws Exception {
        try {
            restTemplate.postForObject(serverAddress + "/api/accounts/add", account, Account.class);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            throw new Exception(extractErrorMessage(e));
        } catch (Exception e) {
            throw new Exception("Connection error: " + e.getMessage());
        }
    }

    public void updateAccount(Account account) throws Exception {
        try {
            restTemplate.put(serverAddress + "/api/accounts/update", account);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            throw new Exception(extractErrorMessage(e));
        } catch (Exception e) {
            throw new Exception("Connection error: " + e.getMessage());
        }
    }

    @Deprecated
    public void addAttendant(ParkingAttendant attendant) throws Exception {
        addAccount(attendant);
    }

    @Deprecated
    public void updateAttendant(ParkingAttendant attendant) throws Exception {
        updateAccount(attendant);
    }

    public void deleteAccount(String username) throws Exception {
        try {
            restTemplate.delete(serverAddress + "/api/accounts/{username}", username);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            throw new Exception(extractErrorMessage(e));
        } catch (Exception e) {
            throw new Exception("Connection error: " + e.getMessage());
        }
    }

    public void updateRates(parkinglot.models.ParkingRate rate) throws Exception {
        try {
            restTemplate.postForObject(serverAddress + "/api/parking/rates", rate, Void.class);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            throw new Exception(extractErrorMessage(e));
        } catch (Exception e) {
            throw new Exception("Connection error: " + e.getMessage());
        }
    }

    // 3. Get Parking Lot Status
    public ParkingLot getStatus() {
        return restTemplate.getForObject(serverAddress + "/api/parking/status", ParkingLot.class);
    }

    public ParkingTicket issueTicket(String license, VehicleType type) {
        org.springframework.web.util.UriComponentsBuilder builder = org.springframework.web.util.UriComponentsBuilder.fromUriString(serverAddress + "/api/parking/issue-ticket")
                .queryParam("license", license);
        if (type != null) {
            builder.queryParam("type", type);
        }
        return restTemplate.postForObject(builder.toUriString(), null, ParkingTicket.class);
    }

    public ParkingTicket getTicket(String ticketNumber) {
        return restTemplate.getForObject(serverAddress + "/api/parking/ticket/{ticketNumber}", ParkingTicket.class, ticketNumber);
    }

    public double calculateFee(String ticketNumber) {
        Double fee = restTemplate.getForObject(serverAddress + "/api/parking/calculate-fee/{ticketNumber}", Double.class, ticketNumber);
        return fee != null ? fee : 0.0;
    }

    public void startCharging(String spotNumber) {
        restTemplate.postForObject(serverAddress + "/api/parking/spots/" + spotNumber + "/charging/start", null, Void.class);
    }

    public void stopCharging(String spotNumber) {
        restTemplate.postForObject(serverAddress + "/api/parking/spots/" + spotNumber + "/charging/stop", null, Void.class);
    }

    public String payTicket(String ticketNumber, double amount, String method) {
        String url = UriComponentsBuilder.fromUriString(serverAddress + "/api/parking/pay")
                .queryParam("ticketNumber", ticketNumber)
                .queryParam("amount", amount)
                .queryParam("method", method)
                .toUriString();
        return restTemplate.postForObject(url, null, String.class);
    }

    public String exitVehicle(String ticketNumber) {
        String url = UriComponentsBuilder.fromUriString(serverAddress + "/api/parking/exit")
                .queryParam("ticketNumber", ticketNumber)
                .toUriString();
        return restTemplate.postForObject(url, null, String.class);
    }

    // --- Admin Parking Management ---

    public void addFloor(String name) throws Exception {
        String url = serverAddress + "/api/parking/floors?name={name}";
        try {
            restTemplate.postForObject(url, null, ParkingFloor.class, name);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            throw new Exception(extractErrorMessage(e));
        } catch (Exception e) {
            throw new Exception("Connection error: " + e.getMessage());
        }
    }

    public void deleteFloor(String name) throws Exception {
        try {
            restTemplate.delete(serverAddress + "/api/parking/floors/{name}", name);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            throw new Exception(extractErrorMessage(e));
        } catch (Exception e) {
            throw new Exception("Connection error: " + e.getMessage());
        }
    }

    public void addSpot(String floorName, String number, ParkingSpotType type) throws Exception {
        String url = serverAddress + "/api/parking/floors/{floorName}/spots?number={number}&type={type}";
        try {
            restTemplate.postForObject(url, null, Void.class, floorName, number, type);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            throw new Exception(extractErrorMessage(e));
        } catch (Exception e) {
            throw new Exception("Connection error: " + e.getMessage());
        }
    }

    public void deleteSpot(String floorName, String spotNumber) throws Exception {
        try {
            restTemplate.delete(serverAddress + "/api/parking/floors/{floorName}/spots/{spotNumber}", floorName, spotNumber);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            throw new Exception(extractErrorMessage(e));
        } catch (Exception e) {
            throw new Exception("Connection error: " + e.getMessage());
        }
    }

    private String extractErrorMessage(org.springframework.web.client.HttpStatusCodeException e) {
        String body = e.getResponseBodyAsString();
        // Log the body to see what the server is actually sending
        System.out.println("Raw Error Response: " + body);
        
        try {
            if (body != null && body.contains("\"message\"")) {
                // Find "message":"
                int msgIndex = body.indexOf("\"message\"");
                int start = body.indexOf(":", msgIndex);
                if (start != -1) {
                    start = body.indexOf("\"", start) + 1;
                    int end = body.indexOf("\"", start);
                    if (end != -1) return body.substring(start, end);
                }
            }
        } catch (Exception ignored) {}
        
        String statusText = e.getStatusText();
        return (statusText != null && !statusText.isEmpty()) ? statusText : "HTTP Error " + e.getStatusCode();
    }

    public void loadTokenFromFile() {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("token.txt");
            if (java.nio.file.Files.exists(path)) {
                String token = java.nio.file.Files.readString(path).trim();
                if (!token.isEmpty()) {
                    this.authToken = token;
                }
            }
        } catch (java.io.IOException e) {
            System.err.println("Could not load token from file: " + e.getMessage());
        }
    }

    private void saveTokenToFile(String token) {
        try (java.io.PrintWriter out = new java.io.PrintWriter("token.txt")) {
            if (token != null) {
                out.print(token);
            }
        } catch (java.io.FileNotFoundException e) {
            System.err.println("Could not save token to file: " + e.getMessage());
        }
    }

    public void loadServerConfig() {
        try {
            System.out.println("Loading server config");
            java.nio.file.Path path = java.nio.file.Paths.get("server_config.txt");
            if (java.nio.file.Files.exists(path)) {
                String content = java.nio.file.Files.readString(path).trim();
                String[] parts = content.split(":");
                if (parts.length == 2) {
                    this.serverAddress = new ServerAddress(parts[0], Integer.parseInt(parts[1]));
                    System.out.println("Loaded: "+ this.serverAddress);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load server config: " + e.getMessage());
        }
    }

    private void saveServerConfig() {
        try (java.io.PrintWriter out = new java.io.PrintWriter("server_config.txt")) {
            out.print(serverAddress.ip + ":" + serverAddress.port);
        } catch (java.io.FileNotFoundException e) {
            System.err.println("Could not save server config: " + e.getMessage());
        }
    }
}