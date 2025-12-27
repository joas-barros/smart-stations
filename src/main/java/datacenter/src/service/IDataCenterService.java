package datacenter.src.service;

import device.src.model.IntegrityPacket;

public interface IDataCenterService {
    IntegrityPacket getAirQualityReport();
    IntegrityPacket getHealthAlerts();
    IntegrityPacket getNoisePollutionReport();
    IntegrityPacket generateThermalComfortReport();
    IntegrityPacket generateTemperatureRanking();

    void registerWithAuthServer();
    void connectToDatabase();
    void startRMIClientService();
    void startEdgeListener();
}
