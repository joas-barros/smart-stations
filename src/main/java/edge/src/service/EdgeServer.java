package edge.src.service;

import auth.app.AppAuth;
import device.src.model.ClimateRecord;
import device.src.model.IntegrityPacket;
import device.src.util.SerializationUtils;
import edge.src.model.LocalDatabase;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DecimalFormat;

public class EdgeServer {


    private static final int BUFFER_SIZE = 4096;

    private static final double MAX_TEMP = 42.0;
    private static final double MAX_CO2 = 1000.0;
    private static final double MAX_UV = 10.0;
    private static final double MAX_NOISE = 90.0;

    private static final String AUTH_SERVER_HOST = "localhost";

    private static LocalDatabase database;
    private int port;

    public EdgeServer(int port) {
        this.port = port;
        this.run();
    }

    public void run() {
        database = new LocalDatabase();

        System.out.println("Servidor de Borda rodando na porta UDP: " + port);

        registerWithAuthServer();

        try (DatagramSocket socket = new DatagramSocket(port)){

            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                try {
                    byte[] receivedData = new byte[packet.getLength()];
                    System.arraycopy(packet.getData(), 0, receivedData, 0, packet.getLength());

                    IntegrityPacket integrityPacket = (IntegrityPacket) SerializationUtils.deserialize(receivedData);

                    if (integrityPacket.isValid()){
                        ClimateRecord record = (ClimateRecord) SerializationUtils.deserialize(integrityPacket.getData());
                        processRecord(record);
                    } else {
                        System.err.println("[ERRO CRÍTICO] Registro climático corrompido recebido. Descartando...");
                    }
                } catch (ClassNotFoundException e) {

                    System.err.println("Erro ao desserializar o registro climático: " + e.getMessage());
                }
            }

        }catch (SocketException e) {
            System.err.println("[ERRO] Não foi possível abrir a porta UDP " + port);
        } catch (IOException e) {
            System.err.println("Erro de I/O no servidor: " + e.getMessage());
        }
    }

    public void registerWithAuthServer() {
        System.out.println("[INIT] Tentando registrar no Servidor de Autenticação...");
        try (Socket socket = new Socket(AUTH_SERVER_HOST, AppAuth.PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // PROTOCOLO: Ajuste conforme o que seu Auth Server espera

            String registerMessage = "REGISTER EDGE " + port;
            System.out.println("[INIT] Mandando requisição ao Auth Server: " + registerMessage);
            out.println(registerMessage);

            String response = in.readLine();
            System.out.println("[INIT] Resposta da Autenticação: " + response);

        } catch (IOException e) {
            System.err.println("[AVISO] Não foi possível contatar o Servidor de Autenticação: " + e.getMessage());
            System.err.println("        Continuando execução, mas o sistema pode estar instável.");
        }
    }

    private void processRecord(ClimateRecord record) {
        DecimalFormat df = new DecimalFormat("#.00");
        System.out.println("[REGISTRO RECEBIDO], Dispositivo:" + record.getDeviceId() + ". Localização: " + record.getLocation() + ". Hora da coleta: " + record.getTimeStamp());

        if (record.getTemperature() > MAX_TEMP) {
            System.err.println("  [ALERTA] (Calor intenso) Temperatura extrema detectada em " + record.getLocation() + ": " + df.format(record.getTemperature()) + "°C");
        }

        if (record.getCarbonDioxide() > MAX_CO2) {
            System.err.println("  [ALERTA] (Má qualidade do ar) Nível de CO2 elevado detectado em " + record.getLocation() + ": " + df.format(record.getCarbonDioxide()) + " ppm");
        }

        if (record.getUvRadiation() > MAX_UV) {
            System.err.println("  [ALERTA] (Radiação muito alto) Índice UV extremo detectado em " + record.getLocation() + ": " + df.format(record.getUvRadiation()));
        }

        if (record.getUrbanNoise() > MAX_NOISE) {
            System.err.println("  [ALERTA] (Poluição sonora grave) Nível de ruído alto detectado em " + record.getLocation() + ": " + df.format(record.getUrbanNoise()) + " dB");
        }

        database.insert(record);

        forwardToDataCenter(record);
    }

    private void forwardToDataCenter(ClimateRecord record) {
        DataCenterForwarder task = new DataCenterForwarder(record);
        new Thread(task).start();
    }
}
