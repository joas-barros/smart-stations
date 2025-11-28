package database.src.service;

import device.src.model.ClimateRecord;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImplDatabaseService extends UnicastRemoteObject implements IDatabaseService{

    private Map<Long, ClimateRecord> climateRecords;
    public ImplDatabaseService() throws RemoteException {
        super();
        climateRecords = new ConcurrentHashMap<>();
    }

    @Override
    public void saveRecord(ClimateRecord record) throws RemoteException {
        climateRecords.put(record.getId(), record);
        System.out.println("[DB] Registro " + record.getId() + " persistido. Total: " + climateRecords.size());
    }

    @Override
    public List<ClimateRecord> getRecords() throws RemoteException {
        System.out.println("[DB] Recuperando todos os registros. Total: " + climateRecords.size());
        return  climateRecords.values().stream().toList();
    }
}
