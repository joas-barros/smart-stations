package auth.src;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Map;

public class AuthThread implements Runnable {

    private Map<String, Integer> storagePorts;
    private Socket clientSocket;

    public AuthThread(Socket clientSocket, Map<String, Integer> storagePorts) {
        this.clientSocket = clientSocket;
        this.storagePorts = storagePorts;
    }
    @Override
    public void run() {
        try {
            // Configura os streams de entrada e saída
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintStream out = new PrintStream(clientSocket.getOutputStream());

            String request = in.readLine();

            if (request == null){
                return; // Cliente desconectado
            }

            if (request.startsWith("REGISTER")) {
                String[] parts = request.split(" ");
                String storageId = parts[1];
                int port = Integer.parseInt(parts[2]);

                // Adiciona a porta do servidor de armazenamento ao mapa
                storagePorts.put(storageId, port);
                System.out.println("Servidor de armazenamento " + storageId + " registrado na porta: " + port);
            } else if (request.startsWith("GET")) {
                String[] parts = request.split(" ");
                String storageId = parts[1];

                // Recupera a porta do servidor de armazenamento solicitado
                Integer port = storagePorts.get(storageId);
                if (port != null) {
                    out.println(port);
                    System.out.println("Porta " + port + " enviada para o cliente para o servidor de armazenamento " + storageId);
                } else {
                    out.println("STORAGE_NOT_FOUND");
                    System.out.println("Servidor de armazenamento " + storageId + " não encontrado.");
                }
            } else {
                out.println("INVALID_REQUEST");
                System.out.println("Requisição inválida recebida: " + request);
            }
        } catch (Exception e) {
            System.err.println("Erro na thread de autenticação: " + e.getMessage());
        }
    }
}
