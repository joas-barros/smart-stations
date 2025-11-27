package auth.src;

import discovery.src.DiscoveryService;

import java.io.PrintStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    // map thread-safe para armazenar as portas de cada serviço
    static Map<String, Integer> storagePorts = new ConcurrentHashMap<>();

    ServerSocket serverSocket;
    Socket client;
    int port;


    public AuthService(int port) {
        this.port = port;
        this.run();
    }

    private void run() {
        try {
            // Cria o socket do servidor na porta especificada
            serverSocket = new ServerSocket(port);
            System.out.println("Servidor de calculo rodando na porta: " + port);

            // Conecta ao servidor diretorio para solicitar o registro da porta
            Socket discoverySocket = new Socket("localhost", DiscoveryService.BASE_PORT);
            System.out.println("Conectado ao servidor de descoberta para registro de porta.");

            // Envia a mensagem de registro
            PrintStream out = new PrintStream(discoverySocket.getOutputStream());
            String registerMessage = "REGISTER " + port;
            out.println(registerMessage);
            System.out.println("Mensagem enviada ao servidor de descoberta: " + registerMessage);

            // Após se registrar, fecha a conexão com o servidor diretorio
            System.out.println("Encerrando conexao com o servidor diretorio...");
            discoverySocket.close();

            // Aguarda por conexões de clientes
            while (true) {
                client = serverSocket.accept();
                System.out.println("Cliente conectado: " + client.getInetAddress().getHostAddress());

                // cria uma nova thread assim que um cliente se conecta
                AuthThread authThread = new AuthThread(client, storagePorts);
                Thread thread = new Thread(authThread);
                thread.start();
            }
        } catch (ConnectException e) {
            System.err.println("Erro ao conectar ao servidor de descoberta: " + e.getMessage());
        }
        catch (Exception e) {
            System.err.println("Erro no servidor de autenticação: " + e.getMessage());
        }
    }
}
