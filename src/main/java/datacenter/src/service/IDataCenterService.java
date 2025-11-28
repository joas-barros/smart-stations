package datacenter.src.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IDataCenterService extends Remote {
    String getAirQualityReport() throws RemoteException;
    String getHealthAlerts() throws RemoteException;
    String getNoisePollutionReport() throws RemoteException;
    String generateThermalComfortReport() throws RemoteException;
    String generateTemperatureRanking() throws RemoteException;
    void registerWithAuthServer() throws  RemoteException;
    void connectToDatabase() throws RemoteException;
    void startRMIClientService() throws RemoteException;
    void startEdgeListener() throws RemoteException;
}
