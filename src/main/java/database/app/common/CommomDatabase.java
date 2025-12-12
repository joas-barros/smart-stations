package database.app.common;

import database.app.AppRemoteDatabaseFollower1;
import database.app.AppRemoteDatabaseFollower2;
import database.app.AppRemoteDatabaseLeader;

import java.util.List;

public class CommomDatabase {
    public static String SERVICE_NAME = "ClimateDB";
    public static List<Integer> AVAILABLE_PORTS = List.of(AppRemoteDatabaseLeader.PORT, AppRemoteDatabaseFollower1.PORT, AppRemoteDatabaseFollower2.PORT);
}
