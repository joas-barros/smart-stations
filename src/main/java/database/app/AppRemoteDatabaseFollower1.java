package database.app;

import database.app.common.CommomDatabase;
import database.src.RemoteDatabaseServer;

public class AppRemoteDatabaseFollower1 {
    public static int PORT = 1100;

    public static void main(String[] args) {
        new RemoteDatabaseServer(PORT, CommomDatabase.SERVICE_NAME, false, CommomDatabase.AVAILABLE_PORTS);
    }
}
