package database.app;

import database.src.RemoteDatabaseServer;

import java.util.ArrayList;
import java.util.List;

public class AppRemoteDatabaseLeader {
    public static int PORT = 1099;
    public static String SERVICE_NAME = "ClimateDB";
    public static List<Integer> FOLLOWER_PORTS = new ArrayList<>();
    static void main() {
        FOLLOWER_PORTS.add(AppRemoteDatabaseFollower1.PORT);
        FOLLOWER_PORTS.add(AppRemoteDatabaseFollower2.PORT);
        new RemoteDatabaseServer(PORT, SERVICE_NAME, true, FOLLOWER_PORTS);
    }
}
