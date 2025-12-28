package auth.src;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthThread implements Runnable {

    private Map<String, List<String>> storagePorts;
    private Socket clientSocket;

    public AuthThread(Socket clientSocket, Map<String, List<String>> storagePorts) {
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
                String port = parts[2];

                String remoteIp = clientSocket.getInetAddress().getHostAddress();

                // Formata o endereço como "IP:PORTA"
                String addressEntry = remoteIp + ":" + port;

                // Adiciona a porta do servidor de armazenamento ao mapa
                storagePorts.computeIfAbsent(serviceType, k -> new ArrayList<>()).add(addressEntry);
                System.out.println("Servidor de armazenamento " + serviceType + " registrado na porta: " + addressEntry);

                // Envia confirmação ao cliente
                out.println("REGISTERED");
            } else if (request.startsWith("GET")) {
                String[] parts = request.split(" ");
                String serviceType = parts[1];

                // Recupera a porta do servidor de armazenamento solicitado
                List<String> addresses = storagePorts.get(serviceType);

                if (addresses != null && !addresses.isEmpty()) {
                    // [SMR] Retorna todas as portas separadas por vírgula (ex: "9090,9091,9092")
                    String response = String.join(",", addresses);
                    out.println(response);
                    System.out.println("Enviada lista de réplicas " + serviceType + ": " + response);
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
