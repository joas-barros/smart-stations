package device.src.service;

import device.src.model.Device;

import java.util.concurrent.atomic.AtomicLong;

public class DeviceService {

    public DeviceService() {
        runService();
    }

    public static final AtomicLong recordId = new AtomicLong(1);

    public void runService(){
        new Thread(new Device("01", "Arduino MKR WiFi 1010", "Santa Delmira")).start();
        new Thread(new Device("02", "Raspberry Pi Pico W", "Nova Betania")).start();
        new Thread(new Device("03", "SSTM32 Blue Pill", "Macarrão")).start();
        new Thread(new Device("04", "NodeMCU-ESP32", "Maisa")).start();

        System.out.println("===========================================================");
        System.out.println("Registros climaticos sendo coletados pelos dispositivos.");
        System.out.println("===========================================================");
    }
}
