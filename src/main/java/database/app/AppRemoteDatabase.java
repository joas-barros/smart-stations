package database.app;

import database.src.RemoteDatabaseServer;

public class AppRemoteDatabase {
    static void main() {
        int port = 1099; // Porta padrão do RMI
        new RemoteDatabaseServer(port);
    }
}
