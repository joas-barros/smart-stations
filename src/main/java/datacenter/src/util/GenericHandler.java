package datacenter.src.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import datacenter.src.service.IDataCenterService;
import device.src.model.IntegrityPacket;
import device.src.util.SerializationUtils;

import java.io.IOException;
import java.io.OutputStream;

public class GenericHandler implements HttpHandler {
    private final IDataCenterService service;
    private final String type;

    public GenericHandler(IDataCenterService service, String type) {
        this.service = service;
        this.type = type;
    }


    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            IntegrityPacket packet;

            // Roteamento simples baseado no tipo
            switch (type) {
                case "air": packet = service.getAirQualityReport(); break;
                case "health": packet = service.getHealthAlerts(); break;
                case "noise": packet = service.getNoisePollutionReport(); break;
                case "thermal": packet = service.generateThermalComfortReport(); break;
                case "ranking": packet = service.generateTemperatureRanking(); break;
                default:
                    httpExchange.sendResponseHeaders(404, -1); // Not Found
                    return;
            }

            if (packet != null) {
                byte[] responseBytes = SerializationUtils.serialize(packet);

                httpExchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
                httpExchange.sendResponseHeaders(200, responseBytes.length);

                OutputStream os = httpExchange.getResponseBody();
                os.write(responseBytes);
                os.close();
                System.out.println("[HTTP] Resposta enviada para endpoint /api/" + type);
            } else {
                String errorMessage = "Erro ao gerar o relatório solicitado.";
                httpExchange.sendResponseHeaders(500, errorMessage.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(errorMessage.getBytes());
                os.close();
                System.err.println("[HTTP] Falha ao gerar o relatório para endpoint /api/" + type);
            }
        } catch (Exception e) {
            String errorMessage = "Erro interno do servidor: " + e.getMessage();
            httpExchange.sendResponseHeaders(500, errorMessage.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(errorMessage.getBytes());
            os.close();
            System.err.println("[HTTP] Exceção ao processar requisição para endpoint /api/" + type + ": " + e.getMessage());
        }
    }
}
