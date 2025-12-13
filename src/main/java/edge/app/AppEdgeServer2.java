package edge.app;

import edge.src.service.EdgeServer;

public class AppEdgeServer2 {
    private static final int EDGE_PORT = 9091;

    public static void main(String[] args) {
        new EdgeServer(EDGE_PORT);
    }
}
