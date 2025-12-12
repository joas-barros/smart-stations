package database.app;

import database.src.RemoteDatabaseServer;

public class AppRemoteDatabaseFollower1 {
    public static int PORT = 1100;
    public static String SERVICE_NAME = "ClimateDB";
    static void main() {
        new RemoteDatabaseServer(PORT, SERVICE_NAME, false, null);
    }
}
