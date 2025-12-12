package database.app;

import database.src.RemoteDatabaseServer;

public class AppRemoteDatabaseFollower2 {
    public static int PORT = 1200;
    public static String SERVICE_NAME = "ClimateDB";
    static void main() {
        new RemoteDatabaseServer(PORT, SERVICE_NAME, false, null);
    }
}
