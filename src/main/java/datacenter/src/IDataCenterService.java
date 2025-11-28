package datacenter.src;

import java.rmi.RemoteException;

public interface IDataCenterService {
    String getAirQualityReport() throws RemoteException;
    String getHealthAlerts() throws RemoteException;
}
