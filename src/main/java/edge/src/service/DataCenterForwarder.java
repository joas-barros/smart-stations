package edge.src.service;

import datacenter.src.DataCenterServer;
import device.src.model.ClimateRecord;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class DataCenterForwarder implements Runnable {

    private static final String DATACENTER_HOST = "localhost";

    private ClimateRecord record;

    public DataCenterForwarder(ClimateRecord record) {
        this.record = record;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(DATACENTER_HOST, DataCenterServer.BASE_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())){

            out.writeObject(record);
            out.flush();

            String response = (String) in.readObject();

            if ("ACK".equals(response)) {
                System.out.println("  [CLOUD] Registro " + record.getId() + " sincronizado com sucesso.");
            } else {
                System.err.println("[CLOUD] Resposta estranha do Data Center: " + response);
            }
        } catch (UnknownHostException e) {
            System.err.println("[CLOUD] Host do Data Center desconhecido: " + DATACENTER_HOST);
        } catch (IOException e) {
            System.err.println("[CLOUD] Erro de I/O ao comunicar com o Data Center: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("[CLOUD] Erro ao ler a resposta do Data Center: " + e.getMessage());
        }
    }
}
