package datacenter.app;

import datacenter.src.DataCenterServer;

public class AppDataCenter {

    public static final int BASE_TCP_PORT = 9000;
    public static final int BASE_RMI_PORT = 9001;
    public static final String BASE_RMI_NAME = "DataCenterService";

    static void main() {
        new DataCenterServer(BASE_TCP_PORT, BASE_RMI_PORT, BASE_RMI_NAME);
    }
}
