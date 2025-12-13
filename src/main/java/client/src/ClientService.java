package client.src;

import datacenter.app.AppDataCenter;
import datacenter.src.service.IDataCenterService;
import device.src.model.IntegrityPacket;
import device.src.util.SerializationUtils;
import discovery.app.AppDiscovery;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.Naming;
import java.util.Scanner;

public class ClientService {
    private static String HOST = "localhost";

    public ClientService() {
        this.run();
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
            int dataCenterRmiPort = requestPortViaTcp("localhost", authPort, "GET DATACENTER");

            if (dataCenterRmiPort == -1) {
                System.err.println("Falha ao localizar Data Center ou serviço offline. Abortando.");
                return;
            }
            System.out.println("[CLIENTE] Data Center localizado na porta RMI: " + dataCenterRmiPort);

            System.out.println("[CLIENTE] Conectando ao serviço de IA via RMI...");

            String rmiUrl = "rmi://" + HOST + ":" + dataCenterRmiPort + "/" + AppDataCenter.BASE_RMI_NAME;
            IDataCenterService service = (IDataCenterService) Naming.lookup(rmiUrl);

            System.out.println("[CLIENTE] Conexão estabelecida! Iniciando interface...");
            showMenu(service);
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

            return Integer.parseInt(response.trim());

        } catch (Exception e) {
            System.err.println("[REDE] Erro ao comunicar com " + host + ":" + port + " - " + e.getMessage());
            return -1;
        }
    }

    private static void showMenu(IDataCenterService service) {
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

            try {
                switch (input) {
                    case "1":
                        System.out.println("\n--- SOLICITANDO ANÁLISE AO DATA CENTER ---");
                        IntegrityPacket packet1 = service.getAirQualityReport();
                        processResponse(packet1);
                        break;

                    case "2":
                        System.out.println("\n--- VERIFICANDO ALERTAS DE RISCO ---");
                        IntegrityPacket packet2 = service.getHealthAlerts();
                        processResponse(packet2);
                        break;

                    case "3":
                        System.out.println("\n--- SOLICITANDO RELATÓRIO DE POLUIÇÃO SONORA ---");
                        IntegrityPacket packet3 = service.getNoisePollutionReport();
                        processResponse(packet3);
                        break;

                    case "4":
                        System.out.println("\n--- GERANDO RELATÓRIO DE CONFORTO TÉRMICO ---");
                        IntegrityPacket packet4 = service.generateThermalComfortReport();
                        processResponse(packet4);
                        break;

                    case "5":
                        System.out.println("\n--- GERANDO RANKING DE TEMPERATURAS ---");
                        IntegrityPacket packet5 = service.generateTemperatureRanking();
                        processResponse(packet5);
                        break;

                    case "0":
                        System.out.println("Encerrando conexão...");
                        running = false;
                        break;

                    default:
                        System.out.println("Opção inválida. Tente novamente.");
                }
            } catch (Exception e) {
                System.err.println("Erro ao comunicar com o servidor: " + e.getMessage());
                System.out.println("Tentando reconectar...");
            }
        }
        scanner.close();
    }
}
