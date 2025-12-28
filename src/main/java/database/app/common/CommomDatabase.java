package database.app.common;

import database.app.AppRemoteDatabaseFollower1;
import database.app.AppRemoteDatabaseFollower2;
import database.app.AppRemoteDatabaseLeader;


import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CommomDatabase {
    public static String SERVICE_NAME = "ClimateDB";

    public static Map<Integer, String> DB_TOPOLOGY = new TreeMap<>();

    static {
        String hostLeader = System.getenv().getOrDefault("DB_HOST_LEADER", "localhost");
        DB_TOPOLOGY.put(AppRemoteDatabaseLeader.PORT, hostLeader);

        String hostFollower1 = System.getenv().getOrDefault("DB_HOST_FOLLOWER1", "localhost");
        DB_TOPOLOGY.put(AppRemoteDatabaseFollower1.PORT, hostFollower1);

        String hostFollower2 = System.getenv().getOrDefault("DB_HOST_FOLLOWER2", "localhost");
        DB_TOPOLOGY.put(AppRemoteDatabaseFollower2.PORT, hostFollower2);
    }

    public static List<Integer> AVAILABLE_PORTS = List.copyOf(DB_TOPOLOGY.keySet());
}
