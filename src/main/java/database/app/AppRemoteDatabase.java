package database.app;

import database.src.RemoteDatabaseServer;

public class AppRemoteDatabase {
    public static int PORT = 1099;
    public static String SERVICE_NAME = "ClimateDB";

    public static void main(String[] args) {
        new RemoteDatabaseServer(PORT, SERVICE_NAME);
    }
}
