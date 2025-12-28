package client.src;

import device.src.model.IntegrityPacket;
import device.src.util.SerializationUtils;
import discovery.app.AppDiscovery;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Scanner;

public class ClientService {
    private static String HOST = "localhost";
    private static int dataCenterPort;
    private static HttpClient httpClient;

    public ClientService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.run();
    }

    private static IntegrityPacket requestData(String endpoint) {
        try {
            String url = "http://" + HOST + ":" + dataCenterPort + "/api/" + endpoint;

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .build();

            System.out.println("[HTTP] Enviando requisição para " + url);

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                byte[] responseBody = response.body();
                return (IntegrityPacket) SerializationUtils.deserialize(responseBody);
            } else {
                System.err.println("[HTTP] Erro na resposta do servidor: Código " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("[HTTP] Falha na comunicação: " + e.getMessage());
            return null;
        }
    }

    private static void processResponse(IntegrityPacket packet) {
        if (packet.isValid()) {
            try {
                // Desembrulha os bytes de volta para String
                String report = (String) SerializationUtils.deserialize(packet.getData());
                System.out.println(report);
            } catch (Exception e) {
                System.err.println("[ERRO] Falha ao ler conteúdo do pacote: " + e.getMessage());
            }
        } else {
            System.err.println("==============================================");
            System.err.println("[ALERTA] INTEGRIDADE COMPROMETIDA!");
            System.err.println("O relatório recebido foi corrompido durante a transmissão.");
            System.err.println("Checksum calculado não confere com o recebido.");
            System.err.println("==============================================");
        }
    }

    public void run(){
        System.out.println(">>> INICIANDO APLICAÇÃO CLIENTE <<<");

        try{
            System.out.println("[Cliente] Contatando Servidor de Descoberta...");
            int authPort = requestPortViaTcp(HOST, AppDiscovery.BASE_PORT, "AUTH_PORT_REQUEST");

            if (authPort == -1) {
                System.err.println("Falha ao localizar Servidor de Autenticação. Abortando.");
                return;
            }

            System.out.println("[CLIENTE] Servidor de Autenticação localizado na porta: " + authPort);

            System.out.println("[CLIENTE] Solicitando endereço do Data Center ao Auth Server...");
            int dataCenterPort = requestPortViaTcp("localhost", authPort, "GET DATACENTER");

            if (dataCenterPort == -1) {
                System.err.println("Falha ao localizar Data Center ou serviço offline. Abortando.");
                return;
            }
            System.out.println("[CLIENTE] Data Center localizado na porta: " + dataCenterPort);
            this.dataCenterPort = dataCenterPort;

            System.out.println("[CLIENTE] Iniciando interface de usuário...");
            showMenu();
        } catch (Exception e) {
            System.err.println("[ERRO CRÍTICO] " + e.getMessage());
        }
    }

    private static int requestPortViaTcp(String host, int port, String message) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Envia a requisição
            out.println(message);

            // Recebe a resposta
            String response = in.readLine();

            if (response == null || response.contains("NOT_FOUND") || response.contains("INVALID")) {
                return -1;
            }

            if (response.contains(":")) {
                String[] parts = response.split(":");
                return Integer.parseInt(parts[1].trim());
            } else {
                return Integer.parseInt(response.trim());
            }

        } catch (Exception e) {
            System.err.println("[REDE] Erro ao comunicar com " + host + ":" + port + " - " + e.getMessage());
            return -1;
        }
    }

    private static void showMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n========================================");
            System.out.println("      SISTEMA DE MONITORAMENTO CLIMÁTICO");
            System.out.println("========================================");
            System.out.println("1. Consultar Relatório de Qualidade do Ar (AQI)");
            System.out.println("2. Verificar Alertas de Saúde (IA)");
            System.out.println("3. Consultar Relatório de Poluição Sonora (dB)");
            System.out.println("4. Gerar Relatório de Conforto Térmico");
            System.out.println("5. Gerar Ranking de Temperaturas");
            System.out.println("0. Sair");
            System.out.print(">>> Escolha uma opção: ");

            String input = scanner.next(); // Usar String evita crash se digitar letras

            // Mapeamento das opções para os endpoints HTTP
            IntegrityPacket packet = null;

            try {
                switch (input) {
                    case "1":
                        System.out.println("\n--- SOLICITANDO ANÁLISE AO DATA CENTER ---");
                        packet = requestData("air-quality");
                        break;

                    case "2":
                        System.out.println("\n--- VERIFICANDO ALERTAS DE RISCO ---");
                        packet = requestData("health-alerts");
                        break;

                    case "3":
                        System.out.println("\n--- SOLICITANDO RELATÓRIO DE POLUIÇÃO SONORA ---");
                        packet = requestData("noise-pollution");
                        break;

                    case "4":
                        System.out.println("\n--- GERANDO RELATÓRIO DE CONFORTO TÉRMICO ---");
                        packet = requestData("thermal-comfort");
                        break;

                    case "5":
                        System.out.println("\n--- GERANDO RANKING DE TEMPERATURAS ---");
                        packet = requestData("temperature-ranking");
                        break;

                    case "0":
                        System.out.println("Encerrando conexão...");
                        running = false;
                        break;

                    default:
                        System.out.println("Opção inválida. Tente novamente.");
                }

                if (packet != null) {
                    processResponse(packet);
                }
            } catch (Exception e) {
                System.err.println("Erro ao comunicar com o servidor: " + e.getMessage());
                System.out.println("Tentando reconectar...");
            }
        }
        scanner.close();
    }
}
