package datacenter.src;

import datacenter.src.service.IDataCenterService;
import datacenter.src.service.ImplDataCenterService;

public class DataCenterServer {

    private int tcpPort;
    private int httpPort;

    public DataCenterServer(int tcpPort, int httpPort) {
        this.tcpPort = tcpPort;
        this.httpPort = httpPort;

        this.run();
    }

    public void run() {
        try {
            System.out.println("[DATACENTER] Iniciando servidor...");

            IDataCenterService server = new ImplDataCenterService(tcpPort,  httpPort);

            // ---------------------------------------------------------------
            // ETAPA 1: Registro no Servidor de Autenticação
            // ---------------------------------------------------------------
            server.registerWithAuthServer();

            // ---------------------------------------------------------------
            // ETAPA 2: Conexão com o Banco de Dados (RPC/RMI)
            // ---------------------------------------------------------------
            server.connectToDatabase();

            // ---------------------------------------------------------------
            // ETAPA 3: Iniciar Recepção de Dados do Edge (TCP)
            // ---------------------------------------------------------------
            server.startEdgeListener();

            // ---------------------------------------------------------------
            // ETAPA 4: Iniciar Servidor http para clientes leves (HTTP)
            // ---------------------------------------------------------------
            server.startHttpServer();
        } catch (Exception e) {
            System.err.println("[DATACENTER] Erro ao iniciar servidor.");
        }
    }
}
