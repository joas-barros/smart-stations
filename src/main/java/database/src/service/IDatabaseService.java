package database.src.service;

import device.src.model.ClimateRecord;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IDatabaseService extends Remote {
    void saveRecord(ClimateRecord record) throws RemoteException;
    List<ClimateRecord> getRecords() throws RemoteException;

    void syncRecord(ClimateRecord record) throws RemoteException;
    void setAsLeader() throws RemoteException;
}
