package datacenter.src.service;

import auth.app.AppAuth;
import database.src.service.IDatabaseService;
import device.src.model.ClimateRecord;

import java.io.*;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ImplDataCenterService extends UnicastRemoteObject implements IDataCenterService{

    private IDatabaseService databaseService;
    private AIService aiService;
    private int myTcpPort;
    private int myRmiPort;

    public ImplDataCenterService(int myTcpPort, int myRmiPort) throws RemoteException {
        this.aiService = new AIService();
        this.myTcpPort = myTcpPort;
        this.myRmiPort = myRmiPort;
    }

    private static final String AUTH_SERVER_HOST = "localhost";

    public void registerWithAuthServer() {
        System.out.println("[INIT] Tentando registrar no Servidor de Autenticação...");
        try (Socket socket = new Socket(AUTH_SERVER_HOST, AppAuth.PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // PROTOCOLO: Ajuste conforme o que seu Auth Server espera
            out.println("REGISTER DATACENTER " + myRmiPort);

            String response = in.readLine();
            System.out.println("[INIT] Resposta da Autenticação: " + response);

        } catch (IOException e) {
            System.err.println("[AVISO] Não foi possível contatar o Servidor de Autenticação: " + e.getMessage());
            System.err.println("        Continuando execução, mas o sistema pode estar instável.");
        }
    }

    public void connectToDatabase() {
        System.out.println("[INIT] Buscando Banco de Dados Remoto...");
        while (databaseService == null) {
            try {
                // Tenta buscar o serviço na porta 1099
                databaseService = (IDatabaseService) Naming.lookup("rmi://localhost:1099/ClimateDB");
                System.out.println("[INIT] Conexão estabelecida com o Banco de Dados.");
            } catch (Exception e) {
                System.out.println("[INIT] Banco de Dados indisponível.");
            }
        }
    }

    public void startRMIClientService() throws RemoteException, MalformedURLException {
        LocateRegistry.createRegistry(myRmiPort);
        Naming.rebind("rmi://localhost:" + myRmiPort + "/DataCenterService", this);
        System.out.println("[RMI] Serviço de IA disponível para clientes na porta " + myRmiPort);
    }

    public void startEdgeListener() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(myTcpPort)) {
                System.out.println("[TCP] Aguardando dados do Edge Server na porta " + myTcpPort);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    // Processa cada conexão em uma thread separada
                    new Thread(() -> handleEdgeData(clientSocket)).start();
                }
            } catch (IOException e) {
                System.err.println("[TCP] Erro crítico no socket TCP: " + e.getMessage());
            }
        }).start();
    }

    private void handleEdgeData(Socket socket) {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            // Recebe
            ClimateRecord record = (ClimateRecord) in.readObject();

            // Persiste
            if (databaseService != null) {
                databaseService.saveRecord(record);
                System.out.println("[DATA] Registro " + record.getId() + " salvo no DB.");
            } else {
                System.err.println("[ERRO] DB desconectado. Dados perdidos.");
            }

            // 3. Confirma
            out.writeObject("ACK");

        } catch (Exception e) {
            System.err.println("[TCP] Erro na transmissão com Edge: " + e.getMessage());
        }
    }


    @Override
    public String getAirQualityReport() throws RemoteException {
        List<ClimateRecord> data = databaseService.getRecords();
        return aiService.generateAirQualityReport(data);
    }

    @Override
    public String getHealthAlerts() throws RemoteException {
        List<ClimateRecord> data = databaseService.getRecords();
        return aiService.generateHealthAlerts(data);
    }

    @Override
    public String getNoisePollutionReport() throws RemoteException {
        List<ClimateRecord> data = databaseService.getRecords();
        return aiService.generateNoisePollutionReport(data);
    }

    @Override
    public String generateThermalComfortReport() throws RemoteException {
        List<ClimateRecord> data = databaseService.getRecords();
        return aiService.generateThermalComfortReport(data);
    }

    @Override
    public String generateTemperatureRanking() throws RemoteException {
        List<ClimateRecord> data = databaseService.getRecords();
        return aiService.generateTemperatureRanking(data);
    }
}
