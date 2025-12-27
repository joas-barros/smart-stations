package datacenter.src.service;

import auth.app.AppAuth;
import database.app.common.CommomDatabase;
import database.src.service.IDatabaseService;
import device.src.model.ClimateRecord;
import device.src.model.IntegrityPacket;
import device.src.util.SerializationUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ImplDataCenterService extends UnicastRemoteObject implements IDataCenterService{

    private IDatabaseService databaseService;
    private AIService aiService;
    private int myTcpPort;
    private int myHttpPort;

    public ImplDataCenterService(int myTcpPort, int myHttpPort, String myRmiName) throws RemoteException {
        this.aiService = new AIService();
        this.myTcpPort = myTcpPort;
        this.myHttpPort = myHttpPort;
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
    public void registerWithAuthServer() {
        System.out.println("[INIT] Tentando registrar no Servidor de Autenticação...");
        try (Socket socket = new Socket(AUTH_SERVER_HOST, AppAuth.PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String requisicao = "REGISTER DATACENTER " + myHttpPort;

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
    public void connectToDatabase() {
        System.out.println("[INIT] Buscando Banco de Dados Remoto...");

        List<Integer> dbPorts = CommomDatabase.AVAILABLE_PORTS;
        while (databaseService == null) {
            for (Integer port : dbPorts) {
                try {
                    databaseService = (IDatabaseService)
                            Naming.lookup("rmi://localhost:" + port + "/" + CommomDatabase.SERVICE_NAME);

                    databaseService.setAsLeader(); // Avisa a réplica: "Agora você manda aqui!"
                    System.out.println("[INIT] Conectado ao Banco de Dados na porta " + port);
                    break;
                } catch (Exception e) {
                    System.err.println("[INIT] [AVISO] Banco de Dados na porta " + port + " indisponível.");
                }
            }
            if (databaseService == null) {
                System.out.println("[AVISO] Nenhum DB encontrado. Tentando novamente em 2s...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void startHttpServer() {

    }

    @Override
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


            // 1. Recebe o objeto
            Object receivedObj = in.readObject();
            if (receivedObj instanceof IntegrityPacket) {
                IntegrityPacket packet = (IntegrityPacket) receivedObj;

                // 2. Valida
                if (packet.isValid()) {
                    ClimateRecord record = (ClimateRecord) SerializationUtils.deserialize(packet.getData());

                    // Salva no DB...
                    if (databaseService != null) {
                        try {
                            databaseService.saveRecord(record);
                        } catch (RemoteException e) {
                            System.err.println("[DATACENTER] Conexão com DB perdida! Tentando reconectar...");
                            this.databaseService = null;
                            connectToDatabase(); // Tenta achar um novo líder (um dos backups)
                            if (databaseService != null) {
                                databaseService.saveRecord(record); // Tenta salvar de novo
                            }
                        }
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

    // Método auxiliar para garantir leitura segura (Failover)
    private List<ClimateRecord> getRecordsSafe() throws RemoteException {
        if (databaseService == null) {
            connectToDatabase();
        }

        try {
            return databaseService.getRecords();
        }  catch (RemoteException e) {
            System.err.println("[DATACENTER] Falha ao ler do Banco de Dados. Tentando reconectar...");
            this.databaseService = null;
            connectToDatabase(); // Tenta achar um novo líder (um dos backups)
            if (databaseService != null) {
                return databaseService.getRecords(); // Tenta buscar de novo
            } else {
                throw new RemoteException("Banco de dados indisponível. Não foi possível gerar o relatório.");
            }
        }
    }


    @Override
    public IntegrityPacket getAirQualityReport() throws RemoteException {
        List<ClimateRecord> data = getRecordsSafe();
        System.out.println("[RMI] Gerando relatório de qualidade do ar...");
        String report = aiService.generateAirQualityReport(data);

        return createPacket(report);
    }

    @Override
    public IntegrityPacket getHealthAlerts() throws RemoteException {
        List<ClimateRecord> data = getRecordsSafe();
        System.out.println("[RMI] Gerando alertas de saúde...");
        String report = aiService.generateHealthAlerts(data);

        return createPacket(report);
    }

    @Override
    public IntegrityPacket getNoisePollutionReport() throws RemoteException {
        List<ClimateRecord> data = getRecordsSafe();
        System.out.println("[RMI] Gerando relatório de poluição sonora...");
        String report = aiService.generateNoisePollutionReport(data);

        return createPacket(report);
    }

    @Override
    public IntegrityPacket generateThermalComfortReport() throws RemoteException {
        List<ClimateRecord> data = getRecordsSafe();
        System.out.println("[RMI] Gerando relatório de conforto térmico...");
        String report = aiService.generateThermalComfortReport(data);

        return createPacket(report);
    }

    @Override
    public IntegrityPacket generateTemperatureRanking() throws RemoteException {
        List<ClimateRecord> data = getRecordsSafe();
        System.out.println("[RMI] Gerando ranking de temperaturas...");
        String report = aiService.generateTemperatureRanking(data);

        return createPacket(report);
    }
}
