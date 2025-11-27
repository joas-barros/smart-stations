package edge.src.service;

import device.src.model.ClimateRecord;
import edge.src.model.LocalDatabase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.DecimalFormat;

public class EdgeServer {

    private static final int EDGE_PORT = 9090;
    private static final int BUFFER_SIZE = 4096;

    private static final double MAX_TEMP = 42.0;
    private static final double MAX_CO2 = 1000.0;
    private static final double MAX_UV = 10.0;
    private static final double MAX_NOISE = 90.0;

    private static LocalDatabase database;

    public EdgeServer() {
        this.run();
    }

    public void run() {
        database = new LocalDatabase();

        System.out.println("Servidor de Borda rodando na porta UDP: " + EDGE_PORT);

        try (DatagramSocket socket = new DatagramSocket(EDGE_PORT)){

            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                try {
                    ByteArrayInputStream byteStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                    ObjectInputStream objectStream = new ObjectInputStream(byteStream);

                    ClimateRecord record = (ClimateRecord) objectStream.readObject();

                    processRecord(record);
                } catch (ClassNotFoundException e) {

                    System.err.println("Erro ao desserializar o registro climático: " + e.getMessage());
                }
            }

        }catch (SocketException e) {
            System.err.println("[ERRO] Não foi possível abrir a porta UDP " + EDGE_PORT);
        } catch (IOException e) {
            System.err.println("Erro de I/O no servidor: " + e.getMessage());
        }
    }

    private void processRecord(ClimateRecord record) {
        DecimalFormat df = new DecimalFormat("#.00");
        System.out.println("[REGISTRO RECEBIDO], Dispositivo:" + record.getDeviceId() + ". Localização: " + record.getLocation() + ". Hora da coleta: " + record.getTimeStamp());

        if (record.getTemperature() > MAX_TEMP) {
            System.out.println("  [ALERTA] (Calor intenso) Temperatura extrema detectada em " + record.getLocation() + ": " + df.format(record.getTemperature()) + "°C");
        }

        if (record.getCarbonDioxide() > MAX_CO2) {
            System.out.println("  [ALERTA] (Má qualidade do ar) Nível de CO2 elevado detectado em " + record.getLocation() + ": " + df.format(record.getCarbonDioxide()) + " ppm");
        }

        if (record.getUvRadiation() > MAX_UV) {
            System.out.println("  [ALERTA] (Radiação muito alto) Índice UV extremo detectado em " + record.getLocation() + ": " + df.format(record.getUvRadiation()));
        }

        if (record.getUrbanNoise() > MAX_NOISE) {
            System.out.println("  [ALERTA] (Poluição sonora grave) Nível de ruído alto detectado em " + record.getLocation() + ": " + df.format(record.getUrbanNoise()) + " dB");
        }

        database.insert(record);

        forwardToDataCenter(record);
    }

    private void forwardToDataCenter(ClimateRecord record) {
        DataCenterForwarder task = new DataCenterForwarder(record);
        new Thread(task).start();
    }
}
