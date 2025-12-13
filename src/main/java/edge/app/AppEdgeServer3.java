package edge.app;

public class AppEdgeServer3 {
    private static final int EDGE_PORT = 9092;

    public static void main(String[] args) {
        new edge.src.service.EdgeServer(EDGE_PORT);
    }
}
