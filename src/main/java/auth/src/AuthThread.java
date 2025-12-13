package auth.src;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthThread implements Runnable {

    private Map<String, List<Integer>> storagePorts;
    private Socket clientSocket;

    public AuthThread(Socket clientSocket, Map<String, List<Integer>> storagePorts) {
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
                String serviceType = parts[1];
                int port = Integer.parseInt(parts[2]);

                // Adiciona a porta do servidor de armazenamento ao mapa
                storagePorts.computeIfAbsent(serviceType, k -> new ArrayList<>()).add(port);
                System.out.println("Servidor de armazenamento " + serviceType + " registrado na porta: " + port);

                // Envia confirmação ao cliente
                out.println("REGISTERED");
            } else if (request.startsWith("GET")) {
                String[] parts = request.split(" ");
                String serviceType = parts[1];

                // Recupera a porta do servidor de armazenamento solicitado
                List<Integer> ports = storagePorts.get(serviceType);
                if (ports != null && !ports.isEmpty()) {
                    // [SMR] Retorna todas as portas separadas por vírgula (ex: "9090,9091,9092")
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < ports.size(); i++) {
                        sb.append(ports.get(i));
                        if (i < ports.size() - 1) sb.append(",");
                    }
                    out.println(sb.toString());
                    System.out.println("Enviada lista de réplicas " + serviceType + ": " + sb);
                } else {
                    out.println("STORAGE_NOT_FOUND");
                    System.out.println("Servidor de armazenamento " + serviceType + " não encontrado.");
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
