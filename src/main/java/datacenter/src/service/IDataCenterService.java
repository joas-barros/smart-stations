package datacenter.src.service;

import device.src.model.IntegrityPacket;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IDataCenterService extends Remote {
    IntegrityPacket getAirQualityReport() throws RemoteException;
    IntegrityPacket getHealthAlerts() throws RemoteException;
    IntegrityPacket getNoisePollutionReport() throws RemoteException;
    IntegrityPacket generateThermalComfortReport() throws RemoteException;
    IntegrityPacket generateTemperatureRanking() throws RemoteException;

    void registerWithAuthServer() throws  RemoteException;
    void connectToDatabase() throws RemoteException;
    void startRMIClientService() throws RemoteException;
    void startEdgeListener() throws RemoteException;
}
