package database.src.service;

import device.src.model.ClimateRecord;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImplDatabaseService extends UnicastRemoteObject implements IDatabaseService{

    private Map<Long, ClimateRecord> climateRecords;

    private List<Integer> backupPorts;
    private boolean isLeader;

    public ImplDatabaseService(boolean isLeader, List<Integer> backupPorts) throws RemoteException {
        super();
        climateRecords = new ConcurrentHashMap<>();
        this.isLeader = isLeader;
        this.backupPorts = backupPorts;
    }

    @Override
    public void saveRecord(ClimateRecord record) throws RemoteException {
        if (climateRecords.containsKey(record.getId())) {
            System.err.println("[DB] Registro duplicado ignorado (SMR): " + record.getId());
            return;
        }

        climateRecords.put(record.getId(), record);
        System.out.println("[DB " + (isLeader ? "LÍDER" : "BACKUP") + "] Registro " + record.getId() + " persistido. Total: " + climateRecords.size());

        if (isLeader && backupPorts != null) {
            propagateToBackups(record);
        }
    }

    private void propagateToBackups(ClimateRecord record) {
        for (Integer port : backupPorts) {
            try {
                IDatabaseService backupService = (IDatabaseService)
                        Naming.lookup("rmi://localhost:" + port + "/ClimateDB");
                backupService.syncRecord(record);
                System.out.println("[DB] Registro " + record.getId() + " sincronizado com backup na porta " + port);
            } catch (Exception e) {
                System.err.println("[DB] Falha ao sincronizar com backup na porta " + port + ": " + e.getMessage());
            }
        }
    }

    @Override
    public List<ClimateRecord> getRecords() throws RemoteException {
        System.out.println("[DB] Recuperando todos os registros. Total: " + climateRecords.size());
        return  climateRecords.values().stream().toList();
    }

    @Override
    public void syncRecord(ClimateRecord record) throws RemoteException {
        if (climateRecords.containsKey(record.getId())) return;

        climateRecords.put(record.getId(), record);
        System.out.println("[DB BACKUP] Sincronizado ID: " + record.getId());
    }

    @Override
    public void setAsLeader() throws RemoteException {
        this.isLeader = true;
        System.err.println("[DB] ATENÇÃO: Fui promovido a LÍDER!");
    }
}
