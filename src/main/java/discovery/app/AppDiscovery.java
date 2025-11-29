package discovery.app;

import discovery.src.DiscoveryService;

public class AppDiscovery {
    public static final int BASE_PORT = 5000;

    public static void main(String[] args) {
        new DiscoveryService(BASE_PORT);
    }
}
