package datacenter.src.service;

import auth.app.AppAuth;
import database.app.AppRemoteDatabase;
import database.src.service.IDatabaseService;
import device.src.model.ClimateRecord;
import device.src.model.IntegrityPacket;
import device.src.util.SerializationUtils;

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
    private String myRmiName;

    public ImplDataCenterService(int myTcpPort, int myRmiPort, String myRmiName) throws RemoteException {
        this.aiService = new AIService();
        this.myTcpPort = myTcpPort;
        this.myRmiPort = myRmiPort;
        this.myRmiName = myRmiName;
    }

    private static final String AUTH_SERVER_HOST = "localhost";

    private IntegrityPacket createPacket(String reportData) throws RemoteException {
        try {
            // 1. Serializa a String do relatório para bytes
            byte[] dataBytes = SerializationUtils.serialize(reportData);
            // 2. Cria o pacote
            return new IntegrityPacket(dataBytes);
        } catch (IOException e) {
            throw new RemoteException("Erro ao serializar resposta no servidor", e);
        }
    }

    @Override
    public void registerWithAuthServer() throws RemoteException {
        System.out.println("[INIT] Tentando registrar no Servidor de Autenticação...");
        try (Socket socket = new Socket(AUTH_SERVER_HOST, AppAuth.PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String requisicao = "REGISTER DATACENTER " + myRmiPort;

            System.out.println("[INIT] Mandando requisição ao Auth Server: " + requisicao);

            // PROTOCOLO: Ajuste conforme o que seu Auth Server espera
            out.println(requisicao);

            String response = in.readLine();
            System.out.println("[INIT] Resposta da Autenticação: " + response);

        } catch (IOException e) {
            System.err.println("[AVISO] Não foi possível contatar o Servidor de Autenticação: " + e.getMessage());
            System.err.println("        Continuando execução, mas o sistema pode estar instável.");
        }
    }

    @Override
    public void connectToDatabase() throws RemoteException {
        System.out.println("[INIT] Buscando Banco de Dados Remoto...");
        while (databaseService == null) {
            try {
                // Tenta buscar o serviço na porta 1099
                databaseService = (IDatabaseService) Naming.lookup("rmi://localhost:" + AppRemoteDatabase.PORT + "/" + AppRemoteDatabase.SERVICE_NAME);
                System.out.println("[INIT] Conexão estabelecida com o Banco de Dados.");
            } catch (Exception e) {
                System.out.println("[INIT] Banco de Dados indisponível.");
            }
        }
    }

    public void startRMIClientService() throws RemoteException {
        LocateRegistry.createRegistry(myRmiPort);
        try {
            Naming.rebind("rmi://localhost:" + myRmiPort + "/" + myRmiName, this);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("[RMI] Serviço de IA disponível para clientes na porta " + myRmiPort);
    }

    @Override
    public void startEdgeListener() throws RemoteException {
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


            // 1. Recebe o objeto
            Object receivedObj = in.readObject();
            if (receivedObj instanceof IntegrityPacket) {
                IntegrityPacket packet = (IntegrityPacket) receivedObj;

                // 2. Valida
                if (packet.isValid()) {
                    ClimateRecord record = (ClimateRecord) SerializationUtils.deserialize(packet.getData());

                    // Salva no DB...
                    if (databaseService != null) {
                        databaseService.saveRecord(record);
                        System.out.println("[DATA] Registro salvo e verificado (CRC OK).");
                    }
                    out.writeObject("ACK");
                } else {
                    System.err.println("[ERRO] Checksum inválido no Datacenter.");
                    out.writeObject("NACK_CHECKSUM_ERROR");
                }
            }

        } catch (Exception e) {
            System.err.println("[TCP] Erro na transmissão com Edge: " + e.getMessage());
        }
    }


    @Override
    public IntegrityPacket getAirQualityReport() throws RemoteException {
        List<ClimateRecord> data = databaseService.getRecords();
        System.out.println("[RMI] Gerando relatório de qualidade do ar...");
        String report = aiService.generateAirQualityReport(data);

        return createPacket(report);
    }

    @Override
    public IntegrityPacket getHealthAlerts() throws RemoteException {
        List<ClimateRecord> data = databaseService.getRecords();
        System.out.println("[RMI] Gerando alertas de saúde...");
        String report = aiService.generateHealthAlerts(data);

        return createPacket(report);
    }

    @Override
    public IntegrityPacket getNoisePollutionReport() throws RemoteException {
        List<ClimateRecord> data = databaseService.getRecords();
        System.out.println("[RMI] Gerando relatório de poluição sonora...");
        String report = aiService.generateNoisePollutionReport(data);

        return createPacket(report);
    }

    @Override
    public IntegrityPacket generateThermalComfortReport() throws RemoteException {
        List<ClimateRecord> data = databaseService.getRecords();
        System.out.println("[RMI] Gerando relatório de conforto térmico...");
        String report = aiService.generateThermalComfortReport(data);

        return createPacket(report);
    }

    @Override
    public IntegrityPacket generateTemperatureRanking() throws RemoteException {
        List<ClimateRecord> data = databaseService.getRecords();
        System.out.println("[RMI] Gerando ranking de temperaturas...");
        String report = aiService.generateTemperatureRanking(data);

        return createPacket(report);
    }
}
