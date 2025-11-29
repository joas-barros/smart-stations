package edge.app;

import edge.src.service.EdgeServer;

public class AppEdgeServer {
    private static final int EDGE_PORT = 9090;

    public static void main(String[] args) {
        new EdgeServer(EDGE_PORT);
    }
}
