package database.app;

import database.src.RemoteDatabaseServer;

public class AppRemoteDatabase {
    public static int PORT = 1099;
    static void main() {
        new RemoteDatabaseServer(PORT);
    }
}
