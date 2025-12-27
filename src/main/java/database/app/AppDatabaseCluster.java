package database.app;

public class AppDatabaseCluster {

    public static void main(String[] args) {
        System.out.println(">>> INICIANDO CLUSTER DE BANCO DE DADOS (3 RÉPLICAS) <<<");

        // Inicia o Líder (Porta 1099)
        new Thread(() -> {
            System.out.println("[CLUSTER] Iniciando Líder...");
            AppRemoteDatabaseLeader.main(args);
        }).start();

        // Inicia o Seguidor 1 (Porta 1100)
        new Thread(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) {} // Pequeno delay visual
            System.out.println("[CLUSTER] Iniciando Follower 1...");
            AppRemoteDatabaseFollower1.main(args);
        }).start();

        // Inicia o Seguidor 2 (Porta 1200)
        new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException e) {} // Pequeno delay visual
            System.out.println("[CLUSTER] Iniciando Follower 2...");
            AppRemoteDatabaseFollower2.main(args);
        }).start();
    }
}