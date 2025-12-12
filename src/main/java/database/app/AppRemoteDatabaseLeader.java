package database.app;

import database.app.common.CommomDatabase;
import database.src.RemoteDatabaseServer;

public class AppRemoteDatabaseLeader {
    public static int PORT = 1099;
    static void main() {
        new RemoteDatabaseServer(PORT, CommomDatabase.SERVICE_NAME, false, CommomDatabase.AVAILABLE_PORTS);
    }
}
