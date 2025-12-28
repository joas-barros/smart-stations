package datacenter.src.service;

import device.src.model.IntegrityPacket;

import java.rmi.RemoteException;

public interface IDataCenterService {
    IntegrityPacket getAirQualityReport() throws RemoteException;
    IntegrityPacket getHealthAlerts() throws RemoteException;
    IntegrityPacket getNoisePollutionReport() throws RemoteException;
    IntegrityPacket generateThermalComfortReport() throws RemoteException;
    IntegrityPacket generateTemperatureRanking() throws RemoteException;

    void registerWithAuthServer();
    void connectToDatabase();
    void startHttpServer();
    void startEdgeListener();
}
