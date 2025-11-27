package device.src.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ClimateRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    // dados do sipositivo
    private String deviceId;
    private LocalDateTime timeStamp;
    private String location;

    // Gases (unidade ppm - partes por milhão)
    // Gases (Geralmente em ppm - partes por milhão)
    private Double carbonDioxide;   // CO2
    private Double carbonMonoxide;  // CO
    private Double nitrogenDioxide; // NO2
    private Double sulfurDioxide;   // SO2

    // Partículas (µg/m³)
    private Double pm25; // Partículas finas
    private Double pm10; // Partículas inaláveis grossas

    // Ambiente
    private Double humidity;    // %
    private Double temperature; // Celsius

    // Outros
    private Double urbanNoise;  // Decibéis (dB)
    private Double uvRadiation; // Índice UV

    public ClimateRecord() {
        this.timeStamp = LocalDateTime.now();
    }

    public ClimateRecord(Long id, String deviceId, String location, Double carbonDioxide, Double carbonMonoxide, Double nitrogenDioxide, Double sulfurDioxide, Double pm25, Double humidity, Double pm10, Double temperature, Double urbanNoise, Double uvRadiation) {
        this.id = id;
        this.deviceId = deviceId;
        this.location = location;
        this.timeStamp = LocalDateTime.now();
        this.carbonDioxide = carbonDioxide;
        this.carbonMonoxide = carbonMonoxide;
        this.nitrogenDioxide = nitrogenDioxide;
        this.sulfurDioxide = sulfurDioxide;
        this.pm25 = pm25;
        this.humidity = humidity;
        this.pm10 = pm10;
        this.temperature = temperature;
        this.urbanNoise = urbanNoise;
        this.uvRadiation = uvRadiation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Double getCarbonDioxide() {
        return carbonDioxide;
    }

    public void setCarbonDioxide(Double carbonDioxide) {
        this.carbonDioxide = carbonDioxide;
    }

    public Double getCarbonMonoxide() {
        return carbonMonoxide;
    }

    public void setCarbonMonoxide(Double carbonMonoxide) {
        this.carbonMonoxide = carbonMonoxide;
    }

    public Double getNitrogenDioxide() {
        return nitrogenDioxide;
    }

    public void setNitrogenDioxide(Double nitrogenDioxide) {
        this.nitrogenDioxide = nitrogenDioxide;
    }

    public Double getSulfurDioxide() {
        return sulfurDioxide;
    }

    public void setSulfurDioxide(Double sulfurDioxide) {
        this.sulfurDioxide = sulfurDioxide;
    }

    public Double getPm25() {
        return pm25;
    }

    public void setPm25(Double pm25) {
        this.pm25 = pm25;
    }

    public Double getPm10() {
        return pm10;
    }

    public void setPm10(Double pm10) {
        this.pm10 = pm10;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getUrbanNoise() {
        return urbanNoise;
    }

    public void setUrbanNoise(Double urbanNoise) {
        this.urbanNoise = urbanNoise;
    }

    public Double getUvRadiation() {
        return uvRadiation;
    }

    public void setUvRadiation(Double uvRadiation) {
        this.uvRadiation = uvRadiation;
    }

    @Override
    public String toString() {
        return "ClimateRecord{" +
                "id=" + id +
                ", deviceId='" + deviceId + '\'' +
                ", timeStamp=" + timeStamp +
                ", carbonDioxide=" + carbonDioxide +
                ", carbonMonoxide=" + carbonMonoxide +
                ", nitrogenDioxide=" + nitrogenDioxide +
                ", sulfurDioxide=" + sulfurDioxide +
                ", pm25=" + pm25 +
                ", pm10=" + pm10 +
                ", humidity=" + humidity +
                ", temperature=" + temperature +
                ", urbanNoise=" + urbanNoise +
                ", uvRadiation=" + uvRadiation +
                '}';
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
