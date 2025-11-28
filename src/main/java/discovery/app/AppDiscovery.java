package discovery.app;

import discovery.src.DiscoveryService;

public class AppDiscovery {
    public static final int BASE_PORT = 5000;
    static void main() {
        new DiscoveryService(BASE_PORT);
    }
}
