package edge.app;

public class AppEdgeServer3 {
    private static final int EDGE_PORT = 9092;

    static void main() {
        new edge.src.service.EdgeServer(EDGE_PORT);
    }
}
