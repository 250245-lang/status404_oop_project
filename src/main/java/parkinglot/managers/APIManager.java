package parkinglot.managers;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import parkinglot.users.Account;
import parkinglot.utils.LoginResponse;

public class APIManager {
    private String authToken = null;
    private ServerAddress serverAddress;
    private final RestTemplate restTemplate = new RestTemplate();

    public APIManager(){
        this.serverAddress = new ServerAddress("127.0.0.1", 8080);
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
    }

    public boolean isLoggedIn() {
        return authToken != null && !authToken.isEmpty();
    }

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
                return response.user();
            }
            return null;
        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
            throw e;
        }
    }
}
