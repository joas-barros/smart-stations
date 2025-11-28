package datacenter.src.service;

import java.rmi.RemoteException;

public interface IDataCenterService {
    String getAirQualityReport() throws RemoteException;
    String getHealthAlerts() throws RemoteException;
}
