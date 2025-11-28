package database.src;

import database.src.service.IDatabaseService;
import database.src.service.ImplDatabaseService;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class RemoteDatabaseServer {
    private int port;
    private String serviceName;

    public RemoteDatabaseServer(int port, String serviceName) {
        this.port = port;
        this.serviceName = serviceName;
        this.run();
    }


    public void run(){

        try {
            IDatabaseService databaseService = new ImplDatabaseService();

            LocateRegistry.createRegistry(port);
            Naming.rebind("rmi://localhost:" + port + "/" + serviceName, databaseService);
            System.out.println("[DB] Servidor RMI iniciado na porta " + port);
        } catch (RemoteException e) {
            System.err.println("[DB] Erro ao iniciar o servidor RMI: " + e.getMessage());
        } catch (MalformedURLException e) {
            System.err.println("[DB] URL malformada: " + e.getMessage());
        }
    }
}
