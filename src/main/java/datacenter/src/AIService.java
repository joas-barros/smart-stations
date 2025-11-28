package datacenter.src;

import device.src.model.ClimateRecord;

import java.util.List;

public class AIService {

    // Relatório de Qualidade do Ar (AQI)
    public String generateAirQualityReport(List<ClimateRecord> records) {
        if (records.isEmpty()) return "IA: Dados insuficientes para gerar modelo de qualidade do ar.";

        double avgCo2 = records.stream().mapToDouble(ClimateRecord::getCarbonDioxide).average().orElse(0);
        double avgPm25 = records.stream().mapToDouble(ClimateRecord::getPm25).average().orElse(0);

        String status;
        if (avgCo2 > 1000 || avgPm25 > 25) status = "CRÍTICO 🔴";
        else if (avgCo2 > 600) status = "MODERADO 🟡";
        else status = "BOM 🟢";

        return String.format(
                "=== RELATÓRIO DE IA (AQI) ===\n" +
                        "Base de Análise: %d amostras\n" +
                        "Concentração Média CO2: %.2f ppm\n" +
                        "Partículas PM2.5: %.2f µg/m³\n" +
                        "Diagnóstico do Sistema: %s",
                records.size(), avgCo2, avgPm25, status
        );
    }

    // Alertas de Saúde Baseados em Padrões
    public String generateHealthAlerts(List<ClimateRecord> records) {
        if (records.isEmpty()) return "IA: Aguardando dados para inferência de saúde.";

        StringBuilder alerts = new StringBuilder("=== PREVISÕES DE SAÚDE (IA) ===\n");
        boolean alertFound = false;

        // Filtra últimos registros para análise de tendência (Simulado pegando todos aqui)
        long highTempCount = records.stream().filter(r -> r.getTemperature() > 35).count();
        long dryAirCount = records.stream().filter(r -> r.getHumidity() < 20).count();

        if (highTempCount > 5) {
            alerts.append("⚠️ ALERTA: Onda de calor detectada. Risco de desidratação em idosos.\n");
            alertFound = true;
        }
        if (dryAirCount > 5) {
            alerts.append("⚠️ ALERTA: Baixa umidade persistente. Risco de doenças respiratórias.\n");
            alertFound = true;
        }

        if (!alertFound) return "IA: Nenhum padrão de risco à saúde identificado no momento.";

        return alerts.toString();
    }
}
