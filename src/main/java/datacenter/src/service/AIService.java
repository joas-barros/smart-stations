package datacenter.src.service;

import device.src.model.ClimateRecord;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AIService {

    // Relatório de Qualidade do Ar (AQI)
    public String generateAirQualityReport(List<ClimateRecord> records) {
        if (records.isEmpty()) return "IA: Dados insuficientes para gerar modelo de qualidade do ar.";

        double avgCo2 = records.stream().mapToDouble(ClimateRecord::getCarbonDioxide).average().orElse(0);
        double avgPm25 = records.stream().mapToDouble(ClimateRecord::getPm25).average().orElse(0);

        String status;
        if (avgCo2 > 1000 || avgPm25 > 25) status = "CRÍTICO";
        else if (avgCo2 > 600) status = "MODERADO";
        else status = "BOM";

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

        long highTempCount = records.stream().filter(r -> r.getTemperature() > 35).count();
        long dryAirCount = records.stream().filter(r -> r.getHumidity() < 20).count();

        if (highTempCount > 5) {
            alerts.append("ALERTA: Onda de calor detectada. Risco de desidratação em idosos.\n");
            alertFound = true;
        }
        if (dryAirCount > 5) {
            alerts.append("ALERTA: Baixa umidade persistente. Risco de doenças respiratórias.\n");
            alertFound = true;
        }

        if (!alertFound) return "IA: Nenhum padrão de risco à saúde identificado no momento.";

        return alerts.toString();
    }

    // Foca exclusivamente no Ruído (dB). Lógica: Classifica o ambiente sonoro
    public String generateNoisePollutionReport(List<ClimateRecord> records) {
        if (records.isEmpty()) return "IA: Sem dados acústicos.";

        double maxNoise = records.stream().mapToDouble(ClimateRecord::getUrbanNoise).max().orElse(0);
        double avgNoise = records.stream().mapToDouble(ClimateRecord::getUrbanNoise).average().orElse(0);

        String impact;
        if (avgNoise > 85) {
            impact = "CRÍTICO: Risco de danos auditivos permanentes.";
        } else if (avgNoise > 65) {
            impact = "ALTO: Estresse psicológico e dificuldade de comunicação.";
        } else if (avgNoise > 50) {
            impact = "MODERADO: Incômodo leve.";
        } else {
            impact = "BAIXO: Ambiente silencioso/residencial.";
        }

        return String.format(
                "=== MAPA DE RUÍDO URBANO ===\n" +
                        "Nível Médio: %.1f dB | Pico Registrado: %.1f dB\n" +
                        "Impacto na População: %s",
                avgNoise, maxNoise, impact
        );
    }

    // Cruza Temperatura e Umidade para calcular a "Sensação Térmica"
    public String generateThermalComfortReport(List<ClimateRecord> records) {
        if (records.isEmpty()) return "IA: Sem dados para análise térmica.";

        // Pega os dados mais recentes
        double avgTemp = records.stream().mapToDouble(ClimateRecord::getTemperature).average().orElse(0);
        double avgHum = records.stream().mapToDouble(ClimateRecord::getHumidity).average().orElse(0);

        String sensation;
        if (avgTemp >= 27 && avgHum > 60) {
            sensation = "MUITO DESCONFORTÁVEL (Risco de Fadiga)";
        } else if (avgTemp >= 32) {
            sensation = "PERIGOSO (Risco de Insolação)";
        } else if (avgTemp < 18) {
            sensation = "FRIO (Necessário Agasalho)";
        } else {
            sensation = "CONFORTÁVEL";
        }

        return String.format(
                "=== RELATÓRIO DE CONFORTO TÉRMICO ===\n" +
                        "Temperatura Média: %.1f°C | Umidade Média: %.1f%%\n" +
                        "Diagnóstico da IA: %s",
                avgTemp, avgHum, sensation
        );
    }

    public String generateTemperatureRanking(List<ClimateRecord> records) {
        if (records.isEmpty()) return "IA: Sem dados suficientes para gerar ranking.";

        Map<String, Double> ranking = records.stream()
                .collect(Collectors.groupingBy(
                        ClimateRecord::getLocation,
                        Collectors.averagingDouble(ClimateRecord::getTemperature)
                ));

        StringBuilder report = new StringBuilder("=== RANKING DE CALOR (MEDIA) ===\n");

        ranking.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed()) // Do maior para o menor
                .forEach(entry -> {
                    String local = entry.getKey();
                    Double temp = entry.getValue();

                    report.append(String.format("%s: %.2f°C\n", local, temp));
                });

        return report.toString();
    }


}
