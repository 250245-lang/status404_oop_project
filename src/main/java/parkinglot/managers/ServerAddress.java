package parkinglot.managers;

public class ServerAddress {
    public final String ip;
    public final int port;
    public final String BASE_URL;

    ServerAddress(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.BASE_URL = "http://"+ip+":"+port;
    }

    @Override
    public String toString() {
        return BASE_URL;
    }
}
