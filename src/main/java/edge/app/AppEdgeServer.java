package edge.app;

import edge.src.service.EdgeServer;

public class AppEdgeServer {
    private static final int EDGE_PORT = 9090;

    static void main() {
        new EdgeServer(EDGE_PORT);
    }
}
