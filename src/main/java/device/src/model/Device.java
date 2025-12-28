package device.src.model;

import device.src.service.DeviceService;
import device.src.util.SerializationUtils;
import discovery.app.AppDiscovery;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Device implements Runnable{
    private String id;
    private String name;
    private String location;

    private static final String DISCOVERY_HOST = System.getenv().getOrDefault("DISCOVERY_HOST", "localhost");

    private static final String AUTH_HOST = System.getenv().getOrDefault("AUTH_HOST", "localhost");

    private final Random random = new Random();

    public Device(String id, String name, String location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    @Override
    public void run() {
        System.out.println("Dispositivo " + id + " iniciado.");

        try {
            int authPort = -1;


            // ETAPA 1: Descoberta (TCP)
            while (authPort == -1) {
                System.out.println("[" + id + "] Conectando ao Servidor de Descoberta...");

                try (Socket discoverySocket = new Socket(DISCOVERY_HOST, AppDiscovery.BASE_PORT);
                     PrintWriter out = new PrintWriter(discoverySocket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(discoverySocket.getInputStream()))) {

                    // Envia solicitação (protocolo simples: string "AUTH_PORT_REQUEST")
                    out.println("AUTH_PORT_REQUEST");

                    // Recebe a porta
                    String response = in.readLine();

                    if (response == null || response.equals("AUTH_PORT_NOT_FOUND")) {
                        System.err.println("[" + id + "] Nenhum servidor Auth disponível. Aguardando 3s...");
                        Thread.sleep(3000);
                    } else {
                        try {
                            authPort = Integer.parseInt(response);
                            System.out.println("[" + id + "] Porta de Autenticação recebida: " + authPort);
                        } catch (NumberFormatException e) {
                            System.err.println("[" + id + "] Resposta inválida do Discovery: " + response);
                            Thread.sleep(3000);
                        }
                    }
                }
            }

            int edgePort = -1;
            System.out.println("[" + id + "] Conectando ao Servidor de Autenticação...");

            // ETAPA 2: Autenticação (TCP) -> Obter lista de portas do Servidor de Borda
            List<InetSocketAddress> targets = new ArrayList<>(); // Lista de endereços completos
            try (Socket authSocket = new Socket(AUTH_HOST, authPort);
                 PrintWriter out = new PrintWriter(authSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(authSocket.getInputStream()))) {

                // Envia ID do dispositivo para autenticar
                out.println("GET EDGE");
                String response = in.readLine(); // Recebe "9090,9091,9092"

                if (response != null && !response.equals("STORAGE_NOT_FOUND")) {
                    for (String entry : response.split(",")) {
                        String[] parts = entry.split(":");
                        String host = parts[0];
                        int port = Integer.parseInt(parts[1]);
                        targets.add(new InetSocketAddress(host, port));
                    }
                    System.out.println("[" + id + "] Réplicas recebidas: " + response);
                }
            }

            // ETAPA 3: Envio de Dados (UDP)
            System.out.println("[" + id + "] Iniciando transmissão UDP para porta " + edgePort);

            DatagramSocket udpSocket = new DatagramSocket(); // Socket para enviar dados

            long startTime = System.currentTimeMillis();
            long duration = 5 * 60 * 1000; // 5 minutos em milissegundos
            long endTime = startTime + duration;

            while (System.currentTimeMillis() < endTime) {
                // 1. Coletar dados (Gera o objeto ClimateRecord)
                ClimateRecord record = collectData();

                // 2. Serializa o ClimateRecord para bytes
                byte[] recordBytes = SerializationUtils.serialize(record);

                // 3. Cria o pacote de integridade (calcula CRC automaticamente)
                IntegrityPacket integrityPacket = new IntegrityPacket(recordBytes);


                // 4. Serializa o IntegrityPacket para enviar via UDP
                byte[] packetBytes = SerializationUtils.serialize(integrityPacket);

                for(InetSocketAddress target : targets) {
                    try {
                        String targetHost = target.getHostString();
                        int targetPort = target.getPort();

                        // 5. Enviar o pacote UDP para cada réplica do Servidor de Borda
                        DatagramPacket udpPacket = new DatagramPacket(
                                packetBytes,
                                packetBytes.length,
                                InetAddress.getByName(targetHost),
                                targetPort
                        );

                        udpSocket.send(udpPacket);
                        System.out.println("Dispositivo " + id + " enviou dados para réplica " + targetPort);
                        System.out.println("  Dados: " + record.toString());
                    } catch (IOException e) {
                        System.err.println("Falha ao enviar para réplica " + target.getPort() + ": " + e.getMessage());
                    }
                }

                // 6. Aguardar 2 a 3 segundos (2000ms a 3000ms)
                int sleepTime = 2000 + random.nextInt(1001); // 2000 + (0 a 1000)
                Thread.sleep(sleepTime);
            }

            udpSocket.close();
            System.out.println("Encerrando dispositivo.");

        } catch (UnknownHostException e) {
            System.err.println("Erro: Host desconhecido. Verifique o endereço do servidor.");
        } catch (IOException e) {
            System.err.println("Erro de I/O na comunicação: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Dispositivo interrompido: " + e.getMessage());
        }
    }

    public ClimateRecord collectData() {
        long uniqueId = DeviceService.recordId.getAndIncrement();

        double co2 = 350 + random.nextDouble() * 100;    // 350 - 450 ppm
        double co = 0.1 + random.nextDouble() * 2;       // 0.1 - 2.1 ppm
        double no2 = 10 + random.nextDouble() * 40;      // 10 - 50 ppb
        double so2 = 1 + random.nextDouble() * 10;       // 1 - 11 ppb

        double pm25 = 5 + random.nextDouble() * 20;      // 5 - 25 µg/m³
        double pm10 = 10 + random.nextDouble() * 40;     // 10 - 50 µg/m³

        double humidity = 30 + random.nextDouble() * 60; // 30% - 90%
        double temp = 20 + random.nextDouble() * 15;     // 20°C - 35°C

        double noise = 40 + random.nextDouble() * 60;    // 40dB - 100dB
        double uv = random.nextDouble() * 11;            // 0 - 11

        return new ClimateRecord(
                uniqueId, this.id, this.location,co2, co, no2, so2, pm25, pm10, humidity, temp, noise, uv
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
