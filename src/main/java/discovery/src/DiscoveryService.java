package discovery.src;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class DiscoveryService {

    private static List<Integer> serverPorts = new CopyOnWriteArrayList<>();

    private static AtomicInteger counter = new AtomicInteger(0);

    private AtomicInteger clientCounter = new AtomicInteger(0);

    private int port;

    public DiscoveryService(int port) {
        this.port = port;
        run();
    }

    private void run() {
        try {
            // Cria o socket do servidor na porta BASE_PORT
            ServerSocket serverSocket = new ServerSocket(port);

            System.out.println("Servidor de diretório rodando na porta: " + port);
            while (true) {
                // Aguarda por conexões de clientes
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nova conexão recebida de: " + clientSocket.getInetAddress().getHostAddress());

                // Cria uma nova thread para lidar com o cliente
                ThreadDiscovery handler = new ThreadDiscovery(clientSocket, serverPorts, counter, clientCounter);
                new Thread(handler).start();
            }
        } catch (Exception e) {
            System.err.println("Erro no servidor de diretório: " + e.getMessage());
        }
    }
}
