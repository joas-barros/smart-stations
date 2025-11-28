package datacenter.src;

import datacenter.src.service.IDataCenterService;
import datacenter.src.service.ImplDataCenterService;

public class DataCenterServer {

    private int tcpPort;
    private int rmiPort;
    private String myRmiName;

    public DataCenterServer(int tcpPort, int rmiPort, String myRmiName) {
        this.tcpPort = tcpPort;
        this.rmiPort = rmiPort;
        this.myRmiName = myRmiName;
        this.run();
    }

    public void run() {
        try {
            System.out.println("[DATACENTER] Iniciando servidor...");

            IDataCenterService server = new ImplDataCenterService(tcpPort,  rmiPort, myRmiName);

            // ---------------------------------------------------------------
            // ETAPA 1: Registro no Servidor de Autenticação
            // ---------------------------------------------------------------
            server.registerWithAuthServer();

            // ---------------------------------------------------------------
            // ETAPA 2: Conexão com o Banco de Dados (RPC/RMI)
            // ---------------------------------------------------------------
            server.connectToDatabase();

            // ---------------------------------------------------------------
            // ETAPA 3: Disponibilizar Serviços para Clientes (RPC/RMI)
            // ---------------------------------------------------------------
            server.startRMIClientService();

            // ---------------------------------------------------------------
            // ETAPA 4: Iniciar Recepção de Dados do Edge (TCP)
            // ---------------------------------------------------------------
            server.startEdgeListener();
        } catch (Exception e) {
            System.err.println("[DATACENTER] Erro ao iniciar servidor.");
        }
    }
}
